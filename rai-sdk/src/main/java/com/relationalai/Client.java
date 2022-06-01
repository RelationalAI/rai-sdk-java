/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.relationalai;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.jsoniter.spi.JsonException;
import com.relationalai.protos.models.MetadataInfoResult;
import com.relationalai.protos.Message;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowStreamReader;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Client {
    public static final String DEFAULT_REGION = "us-east";
    public static final String DEFAULT_SCHEME = "https";
    public static final String DEFAULT_HOST = "azure.relationalai.com";
    public static final int DEFAULT_PORT = 443;

    public String region = DEFAULT_REGION;
    public String scheme = DEFAULT_SCHEME;
    public String host = DEFAULT_HOST;
    public int port = DEFAULT_PORT;
    public Credentials credentials;

    HttpClient httpClient;
    AccessTokenHandler accessTokenHandler;

    static Map<String, String> defaultHeaders = null;

    static {
        defaultHeaders = new HashMap<String, String>();
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("User-Agent", userAgent());
    }

    public Client() {}

    // Note, creating a client from config will also enable the default access
    // token handler, which will cache access tokens in ~/.rai/tokens.json.
    // This behavior can be replaced by callign setAccessTokenHandler with an
    // alternate implementation of AccessTokenHandler handler, or it can be
    // disabled by caling setAccessTokenHandler(null).
    public Client(Config cfg) {
        if (cfg.region != null)
            this.region = cfg.region;
        if (cfg.scheme != null)
            this.scheme = cfg.scheme;
        if (cfg.host != null)
            this.host = cfg.host;
        if (cfg.port != null)
            this.port = Integer.parseInt(cfg.port);
        this.credentials = cfg.credentials;
        this.setAccessTokenHandler(new DefaultAccessTokenHandler());
    }

    // Returns the current `HttpClient` instance, creating one if necessarry.
    HttpClient getHttpClient() {
        if (this.httpClient == null) {
            this.httpClient = HttpClient.newBuilder().build();
        }
        return this.httpClient;
    }

    // Use the HttpClient instance configured by the caller.
    public Client setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public void setAccessTokenHandler(AccessTokenHandler handler) {
        this.accessTokenHandler = handler;
    }

    static final String fetchAccessTokenFormat = "{" +
            "\"client_id\":\"%s\"," +
            "\"client_secret\":\"%s\"," +
            "\"audience\":\"%s\"," +
            "\"grant_type\":\"client_credentials\"}";

    String fetchAccessTokenBody(ClientCredentials credentials) {
        assert credentials != null;
        String audience = String.format("https://%s", this.host);
        return String.format(
                fetchAccessTokenFormat,
                credentials.clientId,
                credentials.clientSecret,
                audience);
    }

    // Fetch the access token from the configured client credentials URL.
    public AccessToken fetchAccessToken(ClientCredentials credentials)
            throws HttpError, InterruptedException, IOException {
        String body = fetchAccessTokenBody(credentials);
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.POST(HttpRequest.BodyPublishers.ofString(body));
        builder.uri(URI.create(credentials.clientCredentialsUrl));
        addHeaders(builder, defaultHeaders);
        HttpRequest request = builder.build();
        HttpResponse<String> response =
                getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        var data = response.body();
        var statusCode = response.statusCode();
        if (statusCode >= 400)
            throw new HttpError(statusCode, data);
        var token = Json.deserialize(data, AccessToken.class);
        var now = Instant.now().toEpochMilli() / 1000l; // epoch secs
        token.createdOn = now;
        return token;
    }

    public AccessToken getAccessToken(ClientCredentials credentials)
            throws HttpError, InterruptedException, IOException {
        var token = credentials.accessToken;
        if (token != null && !token.isExpired())
            return token; // already have it

        if (accessTokenHandler != null)
            token = accessTokenHandler.getAccessToken(this, credentials);
        else
            token = fetchAccessToken(credentials);

        credentials.accessToken = token;
        return token;
    }

    static boolean containsInsensitive(Map<String, String> headers, String key) {
        key = key.toLowerCase();
        for (String k : headers.keySet()) {
            if (k.toLowerCase() == key)
                return true;
        }
        return false;
    }

    // Ensures that the given headers contain the required default values.
    static Map<String, String> ensureHeaders(Map<String, String> headers) {
        if (headers == null)
            return defaultHeaders;
        if (!containsInsensitive(headers, "Accept"))
            headers.put("Accept", "application/json");
        if (!containsInsensitive(headers, "Content-Type"))
            headers.put("Content-Type", "application/json");
        if (!containsInsensitive(headers, "User-Agent"))
            headers.put("User-Agent", userAgent());
        return headers;
    }

    // Encode an element of a query parameter.
    static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    // Ensure the given path is a URL, prefixing with scheme://host:port if
    // needed.
    URI makeUri(String path) {
        return makeUri(path, null);
    }

    // Ensure the given path is a URL, prefixing with scheme://host:port if
    // needed then encode and append the given query params.
    URI makeUri(String path, QueryParams params) {
        var query = params != null ? params.encode() : null;
        try {
            return new URI(this.scheme, null, this.host, this.port, path, query, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.toString());
        }
    }

    // Returns the default User-Agent string.
    static String userAgent() {
        String sdkVersion = SDKProperties.getSDKVersion();
        return String.format("rai-sdk-java/%s", sdkVersion);
    }

    // Returns an HttpRequest.Builder constructed from the given args.
    HttpRequest.Builder newRequestBuilder(String method, String path, QueryParams params) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(makeUri(path, params));
        builder.method(method, BodyPublishers.noBody());
        return builder;
    }

    // Returns an HttpRequest.Builder constructed from the given args.
    HttpRequest.Builder newRequestBuilder(
            String method, String path, QueryParams params, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(makeUri(path, params));
        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return builder;
    }

    void addHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet())
            builder.header(entry.getKey(), entry.getValue());
    }

    // Authenticate the request using the given credentials, if any.
    void authenticate(HttpRequest.Builder builder, Credentials credentials)
            throws HttpError, IOException, InterruptedException {
        if (credentials == null)
            return;
        if (credentials instanceof ClientCredentials) {
            authenticate(builder, (ClientCredentials) credentials);
            return;
        }
        throw new RuntimeException("invalid credential type");
    }

    // Authenticate the given request using the given `ClientCredentials`.
    void authenticate(HttpRequest.Builder builder, ClientCredentials credentials)
            throws HttpError, InterruptedException, IOException {
        AccessToken accessToken = getAccessToken(credentials);
        if (accessToken == null)
            return; // if no token is available, don't authenticate the request
        builder.header("Authorization", String.format("Bearer %s", accessToken.token));
    }

    Object sendRequest(HttpRequest.Builder builder)
            throws HttpError, InterruptedException, IOException {
        addHeaders(builder, defaultHeaders);
        authenticate(builder, this.credentials);
        HttpRequest request = builder.build();
        // printRequest(request);
        HttpResponse<byte[]> response =
                getHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        int statusCode = response.statusCode();
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (statusCode >= 400)
            throw new HttpError(statusCode, new String(response.body(), StandardCharsets.UTF_8));
        if (contentType.toLowerCase().contains("application/json"))
            return new String(response.body(), StandardCharsets.UTF_8);
        else if (contentType.toLowerCase().contains("multipart/form-data")) {
            return MultipartReader.parseMultipartResponse(response);
        } else {
            throw new HttpError(statusCode, "invalid response type");
        }
    }
    private List<ArrowRelation> readArrowFiles(List<TransactionAsyncFile> files) throws IOException {
        var output = new ArrayList<ArrowRelation>();
        for (var file : files) {
            if ("application/vnd.apache.arrow.stream".equals(file.contentType.toLowerCase())) {
                ByteArrayInputStream in = new ByteArrayInputStream(file.data);
                List<FieldVector> fieldVectors = null;
                RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);

                try(ArrowStreamReader arrowStreamReader = new ArrowStreamReader(in, allocator)){
                    VectorSchemaRoot root = arrowStreamReader.getVectorSchemaRoot();
                    fieldVectors = root.getFieldVectors();

                    while(arrowStreamReader.loadNextBatch()) {
                        for(FieldVector fieldVector : fieldVectors) {
                            List<Object> values = new ArrayList<>();

                            for (int i = 0; i < fieldVector.getValueCount(); i ++) {
                                values.add(fieldVector.getObject(i));
                            }
                            output.add(new ArrowRelation(fieldVector.getField().getName(), values));
                        }
                    }
                } finally {
                    if (in != null ) {
                        in.close();
                    }
                    if (fieldVectors != null) {
                        for (FieldVector fieldVector : fieldVectors) {
                            if (fieldVector != null) {
                                fieldVector.close();
                            }
                        }
                    }
                    allocator.close();
                }
            }
        }
        return output;
    }

    private List<Object> parseProblemsResult(String rsp) {
        var output = new ArrayList<Object>();
        var problems = Json.deserialize(rsp).asList();

        for (var problem : problems) {
            var data = Json.serialize(problem);
            try {
                output.add(Json.deserialize(data, IntegrityConstraintViolation.class));
            } catch (JsonException e) {
                output.add(Json.deserialize(data, ClientProblem.class));
            }
        }
        return output;
    }

    private MetadataInfoResult parseMetadataInfo(List<TransactionAsyncFile> metadataInfo) throws InvalidProtocolBufferException {
        Message.MetadataInfo.Builder out = Message.MetadataInfo.newBuilder();
        for (var item : metadataInfo) {
            var relationMetadataList = Message.MetadataInfo.parseFrom(item.data).getRelationsList();
            out.addAllRelations(relationMetadataList);
        }
        return Json.deserialize(JsonFormat.printer().print(out.build()), MetadataInfoResult.class);
    }
    static void printRequest(HttpRequest request) {
        System.out.printf("%s %s\n", request.method(), request.uri());
        for (Map.Entry<String, List<String>> entry : request.headers().map().entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue().get(0);
            System.out.printf("%s: %s\n", k, v);
        }
        // todo: figure out how to get the body from a request (non-trivial)
    }

    public Object request(String method, String path, QueryParams params)
            throws HttpError, InterruptedException, IOException {
        return sendRequest(newRequestBuilder(method, path, params));
    }

    public Object request(String method, String path, QueryParams params, String body)
            throws HttpError, InterruptedException, IOException {
        return sendRequest(newRequestBuilder(method, path, params, body));
    }

    public Object delete(String path)
            throws HttpError, InterruptedException, IOException {
        return delete(path, null, null);
    }

    public Object delete(String path, QueryParams params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("DELETE", path, params, body);
    }

    public Object get(String path)
            throws HttpError, InterruptedException, IOException {
        return get(path, null);
    }

    public Object get(String path, QueryParams params)
            throws HttpError, InterruptedException, IOException {
        return request("GET", path, params);
    }

    public Object patch(String path, QueryParams params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("PATCH", path, params, body);
    }

    public Object post(String path, QueryParams params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("POST", path, params, body);
    }

    public Object put(String path, QueryParams params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("PUT", path, params, body);
    }

    //
    // RAI APIs
    //

    static final String PATH_DATABASE = "/database";
    static final String PATH_ENGINE = "/compute";
    static final String PATH_OAUTH_CLIENTS = "/oauth-clients";
    static final String PATH_TRANSACTION = "/transaction";
    static final String PATH_TRANSACTIONS = "/transactions";
    static final String PATH_USERS = "/users";

    // Returns a URL path constructed from the given parts.
    String makePath(String... parts) {
        return String.join("/", parts);
    }

    // Answers if the given state is one of the terminal states.
    static boolean isTerminalState(String state, String targetState) {
        if (state.equals(targetState))
            return true;
        if (state.contains("FAILED"))
            return true;
        return false;
    }

    //
    // Databases
    //

    public Database createDatabase(String database, String engine)
            throws HttpError, InterruptedException, IOException {
        return createDatabase(database, engine, false);
    }

    public Database createDatabase(
            String database, String engine, boolean overwrite)
            throws HttpError, InterruptedException, IOException {
        var mode = createMode(null, overwrite);
        var tx = new Transaction(this.region, database, engine, mode, false);
        post(PATH_TRANSACTION, tx.queryParams(), tx.payload());
        return getDatabase(database);
    }

    public Database cloneDatabase(
            String database, String engine, String source)
            throws HttpError, InterruptedException, IOException {
        return cloneDatabase(database, engine, source, false);
    }

    public Database cloneDatabase(
            String database, String engine, String source, boolean overwrite)
            throws HttpError, InterruptedException, IOException {
        var mode = createMode(source, overwrite);
        var tx = new Transaction(this.region, database, engine, mode, false, source);
        post(PATH_TRANSACTION, tx.queryParams(), tx.payload());
        return getDatabase(database);
    }

    public DeleteDatabaseResponse deleteDatabase(String database)
            throws HttpError, InterruptedException, IOException {
        var req = new DeleteDatabaseRequest(database);
        var rsp = delete(PATH_DATABASE, null, Json.serialize(req));
        // once this is complete, there is no longer a database resource to return
        return Json.deserialize((String) rsp, DeleteDatabaseResponse.class);
    }

    public Database getDatabase(String database)
            throws HttpError, InterruptedException, IOException {
        var params = new QueryParams();
        params.put("name", database);
        var rsp = get(PATH_DATABASE, params);
        var databases = Json.deserialize((String) rsp, GetDatabaseResponse.class).databases;
        if (databases.length == 0)
            throw new HttpError(404);
        return databases[0];
    }

    public Database[] listDatabases()
            throws HttpError, InterruptedException, IOException {
        return listDatabases(null);
    }

    public Database[] listDatabases(String state)
            throws HttpError, InterruptedException, IOException {
        QueryParams params = null;
        if (state != null) {
            params = new QueryParams();
            params.put("state", state);
        }
        var rsp = get(PATH_DATABASE, params);
        return Json.deserialize((String) rsp, ListDatabasesResponse.class).databases;
    }

    // Engines

    public Engine createEngine(String engine)
            throws HttpError, InterruptedException, IOException {
        return createEngine(engine, null);
    }

    public Engine createEngine(String engine, String size)
            throws HttpError, InterruptedException, IOException {
        if (size == null)
            size = "XS";
        var req = new CreateEngineRequest(this.region, engine, size);
        var rsp = put(PATH_ENGINE, null, Json.serialize(req));
        return Json.deserialize((String) rsp, CreateEngineResponse.class).engine;
    }

    public Engine createEngineWait(String engine)
            throws HttpError, InterruptedException, IOException {
        return createEngineWait(engine, "XS");
    }

    // Create an engine with the given name, and wait for creation to complete.
    public Engine createEngineWait(String engine, String size)
            throws HttpError, InterruptedException, IOException {
        var rsp = createEngine(engine, size);
        while (!isTerminalState(rsp.state, "PROVISIONED")) {
            Thread.sleep(2000);
            rsp = getEngine(engine);
        }
        return rsp;
    }

    public Engine deleteEngine(String engine)
            throws HttpError, InterruptedException, IOException {
        var req = new DeleteEngineRequest(engine);
        delete(PATH_ENGINE, null, Json.serialize(req));
        return getEngine(engine);
    }

    public Engine deleteEngineWait(String engine)
            throws HttpError, InterruptedException, IOException {
        var rsp = deleteEngine(engine);
        while (!isTerminalState(rsp.state, "DELETED")) {
            Thread.sleep(2000);
            rsp = getEngine(engine);
        }
        return rsp;
    }

    public Engine getEngine(String engine)
            throws HttpError, InterruptedException, IOException {
        var params = new QueryParams();
        params.put("name", engine);
        params.put("deleted_on", "");
        var rsp = get(PATH_ENGINE, params);
        var engines = Json.deserialize((String) rsp, GetEngineResponse.class).engines;
        if (engines.length == 0)
            throw new HttpError(404);
        return engines[0];
    }

    public Engine[] listEngines()
            throws HttpError, InterruptedException, IOException {
        return listEngines(null);
    }

    public Engine[] listEngines(String state)
            throws HttpError, InterruptedException, IOException {
        QueryParams params = null;
        if (state != null) {
            params = new QueryParams();
            params.put("state", state);
        }
        var rsp = get(PATH_ENGINE, params);
        return Json.deserialize((String) rsp, ListEnginesResponse.class).engines;
    }

    // OAuth clients

    public OAuthClientExtra createOAuthClient(String name)
            throws HttpError, InterruptedException, IOException {
        return createOAuthClient(name, null);
    }

    public OAuthClientExtra createOAuthClient(String name, String[] permissions)
            throws HttpError, InterruptedException, IOException {
        var req = new CreateOAuthClientRequest(name, permissions);
        var rsp = post(PATH_OAUTH_CLIENTS, null, Json.serialize(req));
        return Json.deserialize((String) rsp, CreateOAuthClientResponse.class).client;
    }

    public DeleteOAuthClientResponse deleteOAuthClient(String id)
            throws HttpError, InterruptedException, IOException {
        var rsp = delete(makePath(PATH_OAUTH_CLIENTS, id));
        return Json.deserialize((String) rsp, DeleteOAuthClientResponse.class);
    }

    public OAuthClient findOAuthClient(String name)
            throws HttpError, InterruptedException, IOException {
        var clients = listOAuthClients();
        for (var client : clients) {
            if (client.name.equals(name))
                return client;
        }
        return null;
    }

    public OAuthClientExtra getOAuthClient(String id)
            throws HttpError, InterruptedException, IOException {
        var rsp = get(makePath(PATH_OAUTH_CLIENTS, id));
        return Json.deserialize((String) rsp, GetOAuthClientResponse.class).client;
    }

    public OAuthClient[] listOAuthClients()
            throws HttpError, InterruptedException, IOException {
        var rsp = get(PATH_OAUTH_CLIENTS);
        return Json.deserialize((String) rsp, ListOAuthClientsResponse.class).clients;
    }

    // Users

    public User createUser(String email)
            throws HttpError, InterruptedException, IOException {
        return createUser(email, null);
    }

    public User createUser(String email, String[] roles)
            throws HttpError, InterruptedException, IOException {
        var req = new CreateUserRequest(email, roles);
        var rsp = post(PATH_USERS, null, Json.serialize(req));
        return Json.deserialize((String) rsp, CreateUserResponse.class).user;
    }

    public DeleteUserResponse deleteUser(String id)
            throws HttpError, InterruptedException, IOException {
        var rsp = delete(makePath(PATH_USERS, id));
        return Json.deserialize((String) rsp, DeleteUserResponse.class);
    }

    public User disableUser(String id)
            throws HttpError, InterruptedException, IOException {
        return updateUser(id, "INACTIVE");
    }

    public User enableUser(String id)
            throws HttpError, InterruptedException, IOException {
        return updateUser(id, "ACTIVE");
    }

    // Returns the User with the given email.
    public User findUser(String email)
            throws HttpError, InterruptedException, IOException {
        var users = listUsers();
        for (var user : users) {
            if (user.email.equals(email))
                return user;
        }
        return null;
    }

    public User getUser(String id)
            throws HttpError, InterruptedException, IOException {
        var rsp = get(makePath(PATH_USERS, id));
        return Json.deserialize((String) rsp, GetUserResponse.class).user;
    }

    public User[] listUsers()
            throws HttpError, InterruptedException, IOException {
        var rsp = get(PATH_USERS);
        return Json.deserialize((String) rsp, ListUsersResponse.class).users;
    }

    public User updateUser(String id, String status)
            throws HttpError, InterruptedException, IOException {
        return updateUser(id, new UpdateUserRequest(status));
    }

    public User updateUser(String id, String[] roles)
            throws HttpError, InterruptedException, IOException {
        return updateUser(id, new UpdateUserRequest(roles));
    }

    public User updateUser(String id, String status, String[] roles)
            throws HttpError, InterruptedException, IOException {
        return updateUser(id, new UpdateUserRequest(status, roles));
    }

    public User updateUser(String id, UpdateUserRequest req)
            throws HttpError, InterruptedException, IOException {
        var rsp = patch(makePath(PATH_USERS, id), null, Json.serialize(req));
        return Json.deserialize((String) rsp, UpdateUserResponse.class).user;
    }

    // Transactions

    String createMode(String source, boolean overwrite) {
        if (source != null)
            return overwrite ? "CLONE_OVERWRITE" : "CLONE";
        else
            return overwrite ? "CREATE_OVERWRITE" : "CREATE";
    }

    public TransactionResult execute(String database, String engine, String source)
            throws HttpError, InterruptedException, IOException {
        return execute(database, engine, source, false, null);
    }

    public TransactionResult execute(
            String database, String engine, String source, boolean readonly)
            throws HttpError, InterruptedException, IOException {
        return execute(database, engine, source, readonly, null);
    }

    public TransactionResult execute(
            String database, String engine,
            String source, boolean readonly,
            Map<String, String> inputs)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(region, database, engine, "OPEN", readonly);
        var action = DbAction.makeQueryAction(source, inputs);
        var body = tx.payload(action);
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), body);
        return Json.deserialize((String) rsp, TransactionResult.class);
    }

    public TransactionAsyncResult executeAsyncWait(
            String database, String engine, String source, boolean readonly) throws HttpError, IOException, InterruptedException {
        return executeAsyncWait(database, engine, source, readonly, new HashMap<>());
    }

    public TransactionAsyncResult executeAsyncWait(
            String database, String engine,
            String source, boolean readonly,
            Map<String, String> inputs) throws HttpError, IOException, InterruptedException {

        var id = executeAsync(database, engine, source, readonly, inputs).transaction.id;

        var transaction = getTransaction(id).transaction;

        while ( !("COMPLETED".equals(transaction.state) || "ABORTED".equals(transaction.state)) ) {
            Thread.sleep(2000);
            transaction = getTransaction(id).transaction;
        }

        var results = getTransactionResults(id);
        var metadata = getTransactionMetadata(id);
        var problems = getTransactionProblems(id);

        return new TransactionAsyncResult(
                transaction,
                results,
                metadata,
                null, // todo add getMetadataInfo (check if separate endpoint exists)
                problems
        );
    }

    public TransactionAsyncResult executeAsync(
            String database, String engine, String source, boolean readonly) throws HttpError, IOException, InterruptedException {
        return executeAsync(database, engine, source, readonly, new HashMap<>());
    }

    public TransactionAsyncResult executeAsync(
            String database, String engine,
            String source, boolean readonly,
            Map<String, String> inputs) throws HttpError, IOException, InterruptedException {
        var tx = new TransactionAsync(database, engine,  source, readonly, inputs);
        var body = tx.payload();
        var rsp = post(PATH_TRANSACTIONS, tx.queryParams(), body);
        if (rsp instanceof String) {
            var txn = Json.deserialize((String) rsp, TransactionAsyncCompactResponse.class);
            return new TransactionAsyncResult(txn, new ArrayList<ArrowRelation>(), new ArrayList<TransactionAsyncMetadataResponse>(), null, new ArrayList<Object>());
        }
        return readTransactionAsyncResults((List<TransactionAsyncFile>) rsp);
    }

    private TransactionAsyncResult readTransactionAsyncResults(List<TransactionAsyncFile> files) throws HttpError, IOException {
        var transaction = files
                .stream().
                filter(f -> f.name.equals("transaction"))
                .collect(Collectors.toList());
        var metadata = files
                .stream()
                .filter(f -> f.name.equals("metadata"))
                .collect(Collectors.toList());
        var metadataInfo = files
                .stream()
                .filter(f -> f.name.equals("metadata_info"))
                .collect(Collectors.toList());
        var problems = files
                .stream()
                .filter(f -> f.name.equals("problems"))
                .collect(Collectors.toList());

        if (transaction.isEmpty()) {
            throw new HttpError(404, "transaction part is missing");
        }
        var transactionResponse = Json.deserialize(new String(transaction.get(0).data, StandardCharsets.UTF_8), TransactionAsyncCompactResponse.class);

        if (metadata.isEmpty()) {
            throw new HttpError(404, "metadata part is missing");
        }
        var metadataResponse = Json.deserialize(new String(metadata.get(0).data, StandardCharsets.UTF_8), TransactionAsyncMetadataResponse[].class);

        if (metadataInfo.isEmpty()) {
            throw new HttpError(404, "metadata info part is missing");
        }
        var metadataInfoResult = parseMetadataInfo(metadataInfo);

        if (problems.isEmpty()) {
            throw new HttpError(404, "problems part is missing");
        }
        var problemsResult = parseProblemsResult(new String(problems.get(0).data, StandardCharsets.UTF_8));

        var results = readArrowFiles(files);
        return new TransactionAsyncResult(
                transactionResponse,
                results,
                Arrays.asList(metadataResponse),
                metadataInfoResult,
                problemsResult
        );
    }

    public TransactionAsyncSingleResponse getTransaction(String id) throws HttpError, IOException, InterruptedException {
        var rsp = get(String.format("%s/%s", PATH_TRANSACTIONS, id));
        return Json.deserialize((String) rsp, TransactionAsyncSingleResponse.class);
    }

    public TransactionsAsyncMultipleResponses getTransactions() throws HttpError, IOException, InterruptedException {
        var rsp = get(PATH_TRANSACTIONS);
        return Json.deserialize((String) rsp,TransactionsAsyncMultipleResponses.class);
    }

    public List<ArrowRelation> getTransactionResults(String id) throws HttpError, IOException, InterruptedException {
        var rsp = (List<TransactionAsyncFile>) get(String.format("%s/%s/results", PATH_TRANSACTIONS, id));
        return readArrowFiles(rsp);
    }

    public List<TransactionAsyncMetadataResponse> getTransactionMetadata(String id) throws HttpError, IOException, InterruptedException {
        var rsp = get(String.format("%s/%s/metadata", PATH_TRANSACTIONS, id));
        var results = Json.deserialize((String) rsp, TransactionAsyncMetadataResponse[].class);
        return Arrays.asList(results);
    }

    public MetadataInfoResult getTransactionMetadataInfo(String id) throws HttpError, IOException, InterruptedException {
        var rsp = get(String.format("%s/%s/metadata_info", PATH_TRANSACTIONS, id));
        System.out.println(rsp);
        return null;
    }

    public List<Object> getTransactionProblems(String id) throws HttpError, IOException, InterruptedException {
        var rsp = (String) get(String.format("%s/%s/problems", PATH_TRANSACTIONS, id));
        return parseProblemsResult(rsp);
    }

    public TransactionAsyncDeleteResponse deleteTransaction(String id) throws HttpError, IOException, InterruptedException {
        var rsp = (String) delete(String.format("%s/%s", PATH_TRANSACTIONS, id));
        return Json.deserialize(rsp, TransactionAsyncDeleteResponse.class);
    }

    // EDBs

    public Edb[] listEdbs(String database, String engine)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(this.region, database, engine, "OPEN", true);
        var action = DbAction.makeListEdbAction();
        var body = tx.payload(action);
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), body);
        var actions = Json.deserialize((String) rsp, ListEdbsResponse.class).actions;
        if (actions.length == 0)
            return new Edb[] {};
        return actions[0].result.rels;
    }

    // Models

    // Delete the named model.
    public TransactionResult deleteModel(String database, String engine, String name)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(this.region, database, engine, "OPEN");
        var action = DbAction.makeDeleteModelAction(name);
        var body = tx.payload(action);
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), body);
        return Json.deserialize((String) rsp, TransactionResult.class);
    }

    // Delete the list of named models.
    public TransactionResult deleteModel(String database, String engine, String[] names)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(this.region, database, engine, "OPEN");
        var actions = DbAction.makeDeleteModelsAction(names);
        var body = tx.payload(actions);
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), body);
        return Json.deserialize((String) rsp, TransactionResult.class);
    }

    // Return the named model.
    public Model getModel(String database, String engine, String name)
            throws HttpError, InterruptedException, IOException {
        var models = listModels(database, engine);
        for (var item : models) {
            if (item.name.equals(name))
                return item;
        }
        throw new HttpError(404);
    }

    // Load a model into the given database.
    public TransactionResult loadModel(
            String database, String engine, String name, InputStream model)
            throws HttpError, InterruptedException, IOException {
        var s = new String(model.readAllBytes());
        return loadModel(database, engine, name, s);
    }

    public TransactionResult loadModel(
            String database, String engine, String name, String model)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(this.region, database, engine, "OPEN", false);
        var action = DbAction.makeInstallAction(name, model);
        var data = tx.payload(action);
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), data);
        return Json.deserialize((String) rsp, TransactionResult.class);
    }

    // Load multiple models into the given database.
    public TransactionResult loadModels(
            String database, String engine, Map<String, String> models)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(this.region, database, engine, "OPEN", false);
        var actions = DbAction.makeInstallAction(models);
        var data = tx.payload(actions);
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), data);
        return Json.deserialize((String) rsp, TransactionResult.class);
    }

    // Returns the list of names of models installed in the given database.
    public String[] listModelNames(String database, String engine)
            throws HttpError, InterruptedException, IOException {
        var models = listModels(database, engine);
        String[] result = new String[models.length];
        for (var i = 0; i < models.length; ++i)
            result[i] = models[i].name;
        return result;
    }

    // Returns the list of models (including source) installed in the given
    // database.
    public Model[] listModels(String database, String engine)
            throws HttpError, InterruptedException, IOException {
        var tx = new Transaction(this.region, database, engine, "OPEN", true);
        var body = tx.payload(DbAction.makeListModelsAction());
        var rsp = post(PATH_TRANSACTION, tx.queryParams(), body);
        var actions = Json.deserialize((String) rsp, ListModelsResponse.class).actions;
        if (actions.length == 0)
            return new Model[] {};
        return actions[0].result.models;
    }

    // Data loading

    static void genSchemaConfig(StringBuilder builder, CsvOptions options) {
        if (options == null)
            return;
        var schema = options.schema;
        if (schema == null || schema.isEmpty())
            return;
        var count = 0;
        builder.append("def config:schema =");
        for (var entry : schema.entrySet()) {
            if (count > 0)
                builder.append(';');
            var k = entry.getKey();
            var v = entry.getValue();
            builder.append(String.format("\n    :%s, \"%s\"", k, v));
            count++;
        }
        builder.append('\n');
    }

    // Returns a Rel literal for the given value.
    static String genLiteral(int value) {
        return Integer.toString(value);
    }

    // Returns a Rel literal for the given value.
    static String genLiteral(char value) {
        if (value == '\'')
            return "'\\''";
        return String.format("'%c'", value);
    }

    // Returns a Rel literal for the given value.
    static String genLiteral(Object value) {
        assert value != null;
        if (value instanceof Integer)
            return genLiteral((int) value);
        if (value instanceof Character)
            return genLiteral((char) value);
        assert false;
        return null;
    }

    // Returns a Rel syntax config def for the given option name and value.
    static void genSyntaxOption(StringBuilder builder, String name, Object value) {
        if (value == null)
            return;
        var lit = genLiteral(value);
        var def = String.format("def config:syntax:%s = %s\n", name, lit);
        builder.append(def);
    }

    // Generate Rel config defs for the given CSV options.
    static void genSyntaxConfig(StringBuilder builder, CsvOptions options) {
        if (options == null)
            return;
        genSyntaxOption(builder, "header_row", options.headerRow);
        genSyntaxOption(builder, "delim", options.delim);
        genSyntaxOption(builder, "escapechar", options.escapeChar);
        genSyntaxOption(builder, "quotechar", options.quoteChar);
    }

    // Generate Rel to load CSV data into a relation with the given options.
    static String genLoadCsv(String relation, CsvOptions options) {
        var builder = new StringBuilder();
        genSchemaConfig(builder, options);
        genSyntaxConfig(builder, options);
        builder.append("def config:data = data\n");
        builder.append(String.format("def insert:%s = load_csv[config]", relation));
        return builder.toString();
    }

    public TransactionResult loadCsv(
            String database, String engine, String relation, InputStream data)
            throws HttpError, InterruptedException, IOException {
        var s = new String(data.readAllBytes());
        return loadCsv(database, engine, relation, s, null);
    }

    public TransactionResult loadCsv(
            String database, String engine, String relation, String data)
            throws HttpError, InterruptedException, IOException {
        return loadCsv(database, engine, relation, data, null);
    }

    public TransactionResult loadCsv(
            String database, String engine, String relation,
            InputStream data, CsvOptions options)
            throws HttpError, InterruptedException, IOException {
        var s = new String(data.readAllBytes());
        return loadCsv(database, engine, relation, s, options);
    }

    public TransactionResult loadCsv(
            String database, String engine, String relation,
            String data, CsvOptions options)
            throws HttpError, InterruptedException, IOException {
        var source = genLoadCsv(relation, options);
        var inputs = new HashMap<String, String>();
        inputs.put("data", data);
        return execute(database, engine, source, false, inputs);
    }

    // Generate the Rel to load JSON data into a relation.
    static String genLoadJson(String relation) {
        var builder = new StringBuilder();
        builder.append("def config:data = data\n");
        builder.append(String.format("def insert:%s = load_json[config]", relation));
        return builder.toString();
    }

    public TransactionResult loadJson(
            String database, String engine, String relation, InputStream data)
            throws HttpError, InterruptedException, IOException {
        var s = new String(data.readAllBytes());
        return loadJson(database, engine, relation, s);
    }

    public TransactionResult loadJson(
            String database, String engine, String relation, String data)
            throws HttpError, InterruptedException, IOException {
        var inputs = new HashMap<String, String>();
        inputs.put("data", data);
        var source = genLoadJson(relation);
        return execute(database, engine, source, false, inputs);
    }
}
