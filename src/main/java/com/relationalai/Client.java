/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.relationalai;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    public static final String DEFAULT_REGION = "us-east";
    public static final String DEFAULT_SCHEME = "https";
    public static final String DEFAULT_HOST = "azure.relationalai.com";
    public static final String DEFAULT_PORT = "443";

    public String region = DEFAULT_REGION;
    public String scheme = DEFAULT_SCHEME;
    public String host = DEFAULT_HOST;
    public String port = DEFAULT_PORT;
    public Credentials credentials;

    HttpClient httpClient;

    public Client() {
    }

    public Client(String region, String scheme, String host, String port, Credentials credentials) {
        this.region = region;
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.credentials = credentials;
    }

    public Client(Config cfg) {
        if (cfg.region != null)
            this.region = cfg.region;
        if (cfg.scheme != null)
            this.scheme = cfg.scheme;
        if (cfg.host != null)
            this.host = cfg.host;
        if (cfg.port != null)
            this.port = cfg.port;
        this.credentials = cfg.credentials;
    }

    // Returns the current `HttpClient` instance, creating one if necessarry.
    HttpClient httpClient() {
        if (this.httpClient == null) {
            this.httpClient = HttpClient.newBuilder().build();
        }
        return this.httpClient;
    }

    // Use the HttpClient instance configured by the caller.
    public void withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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

    public AccessToken fetchAccessToken(ClientCredentials credentials)
            throws HttpError, InterruptedException, IOException {
        String body = fetchAccessTokenBody(credentials);
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.POST(HttpRequest.BodyPublishers.ofString(body));
        builder.uri(URI.create(credentials.clientCredentialsUrl));
        addHeaders(builder, defaultHeaders);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode >= 400 && statusCode < 500)
            throw new HttpError(statusCode, response.body());
        return AccessToken.fromJson(response.body());
    }

    // todo: add callback func
    public AccessToken getAccessToken(ClientCredentials credentials)
            throws HttpError, InterruptedException, IOException {
        AccessToken accessToken = credentials.accessToken;
        if (accessToken == null || accessToken.isExpired()) {
            accessToken = fetchAccessToken(credentials);
            credentials.accessToken = accessToken;
        }
        credentials.accessToken = fetchAccessToken(credentials);
        return accessToken;
    }

    static Boolean containsInsensitive(Map<String, String> headers, String key) {
        key = key.toLowerCase();
        for (String k : headers.keySet()) {
            if (k.toLowerCase() == key)
                return true;
        }
        return false;
    }

    static Map<String, String> defaultHeaders = new HashMap<String, String>() {
        {
            put("Accept", "application/json");
            put("Content-Type", "application/json");
            put("User-Agent", userAgent());
        }
    };

    // Ensures that the given headers contain the required default values.
    static Map<String, String> defaultHeaders(Map<String, String> headers) {
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
    static String encodeParam(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    // Ensure the given path is a URL, prefixing with scheme://host:port if
    // needed.
    String makeUrl(String path) {
        if (path.startsWith("/")) {
            path = String.format("%s://%s:%s%s", scheme, host, port, path);
        }
        return path;
    }

    // Ensure the given path is a URL, prefixing with scheme://host:port if
    // needed, encode at append the given query params.
    String makeUrl(String path, Map<String, String> params) throws UnsupportedEncodingException {
        path = makeUrl(path);
        if (params == null)
            return path;
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0)
                query.append('&');
            query.append(encodeParam(entry.getKey()));
            query.append('=');
            query.append(encodeParam(entry.getValue()));
        }
        return path + "?" + query.toString();
    }

    // Returns the default User-Agent string.
    static String userAgent() {
        return String.format("rai-sdk-java/%s", "0.0.1"); // todo: version
    }

    // Returns an HttpRequest.Builder constructed from the given args.
    HttpRequest.Builder newRequestBuilder(String method, String path, Map<String, String> params)
            throws UnsupportedEncodingException {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(makeUrl(path, params)));
        builder.method(method, BodyPublishers.noBody());
        return builder;
    }

    // Returns an HttpRequest.Builder constructed from the given args.
    HttpRequest.Builder newRequestBuilder(String method, String path, Map<String, String> params, String body)
            throws UnsupportedEncodingException {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(makeUrl(path, params)));
        builder.method(method, HttpRequest.BodyPublishers.ofString(body));
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
        builder.header("Authorization", String.format("Bearer %s", accessToken.token));
    }

    String sendRequest(HttpRequest.Builder builder)
            throws HttpError, InterruptedException, IOException {
        addHeaders(builder, defaultHeaders);
        authenticate(builder, this.credentials);
        HttpRequest request = builder.build();
        // printRequest(request);
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode >= 400 && statusCode < 500)
            throw new HttpError(statusCode, response.body());
        return response.body();
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

    public String request(String method, String path, Map<String, String> params)
            throws HttpError, InterruptedException, IOException {
        return sendRequest(newRequestBuilder(method, path, params));
    }

    public String request(String method, String path, Map<String, String> params, String body)
            throws HttpError, InterruptedException, IOException {
        return sendRequest(newRequestBuilder(method, path, params, body));
    }

    public String delete(String path, Map<String, String> params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("DELETE", path, params, body);
    }

    public String get(String path, Map<String, String> params)
            throws HttpError, InterruptedException, IOException {
        return request("GET", path, params);
    }

    public String patch(String path, Map<String, String> params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("PATCH", path, params, body);
    }

    public String post(String path, Map<String, String> params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("POST", path, params, body);
    }

    public String put(String path, Map<String, String> params, String body)
            throws HttpError, InterruptedException, IOException {
        return request("PUT", path, params, body);
    }

    //
    // REST APIs
    //

    static final String PATH_DATABASE = "/database";
    static final String PATH_ENGINE = "/compute";
    static final String PATH_OAUTH_CLIENTS = "/oauth-clients";
    static final String PATH_TRANSACTION = "/transaction";
    static final String PATH_USERS = "/users";

    // Databases

    // todo: filters
    public ListDatabasesResponse listDatabases()
            throws HttpError, InterruptedException, IOException {
        String rsp = get(PATH_DATABASE, null);
        return ListDatabasesResponse.fromJson(rsp);
    }

    // Engines

    // Models

    // OAuth clients

    // Transactions

    public List<Edb> listEdbs(String database, String engine) {
        return null;
    }

    public JSONObject exec(String database, String engine, String source) {
        return exec(database, engine, source, false, null);
    }

    public JSONObject exec(String database, String engine, String source, Boolean readonly) {
        return exec(database, engine, source, readonly, null);
    }

    public JSONObject exec(String datbabase, String engine, String source, Boolean readonly,
            Map<String, String> inputs) {
        return null;
    }

    void execTest() {
        JSONObject rsp = exec("bradlo-test", "bradlo-test", "def output = 1, 2, 3; 4, 5, 6");
        try {
            System.out.println(rsp.toString(4));
        } catch (JSONException e) {
            System.out.println(e.toString());
        }
    }

    // *** temporary ***

    void testModels() {
        JSONArray array;

        array = new JSONArray("[\"foo\",\"bar\",\"biz\",\"baz\"]");
        List<String> resultString = Model.asStringList(array);
        System.out.println(resultString);

        array = new JSONArray("[1, 2, 3, 4]");
        List<Integer> resultInt = Model.asIntList(array);
        System.out.println(resultInt);

        array = new JSONArray("[{\"name\":\"foo\"},{\"name\":\"bar\"}]");
        List<Edb> resultEdb = Model.asModelList(array, Edb.class);
        System.out.println(resultEdb);
    }

    public void run() throws HttpError, InterruptedException, IOException {
        var cfg = Config.loadConfig("~/.rai/config");
        var client = new Client(cfg);
        var rsp = client.listDatabases();
        for (var database : rsp.Databases) {
            System.out.printf("%s (%s)\n", database.Name, database.AccountName);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.run();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
