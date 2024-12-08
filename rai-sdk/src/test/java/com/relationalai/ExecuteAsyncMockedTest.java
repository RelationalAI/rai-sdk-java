/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.relationalai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import relationalai.protocol.Message;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({TestExtension.class})
public class ExecuteAsyncMockedTest extends UnitTest {

    @Test
    void testExecuteAsyncMocked() throws HttpError, InterruptedException, IOException, URISyntaxException {
        var client = createClient();
        client.setHttpClient(getMockedHttpClient());

        var query = "x, x^2, x^3, x^4 from x in {1; 2; 3; 4; 5}";

        var rsp = client.executeAsync("mockedDB", "mockedEngine", query, true);

        var results = new ArrayList<ArrowRelation> () {
            {
                add(new ArrowRelation("", Arrays.asList(new Object[] {1L, 2L, 3L, 4L, 5L}) ));
                add(new ArrowRelation("", Arrays.asList(new Object[] {1L, 4L, 9L, 16L, 25L}) ));
                add(new ArrowRelation("", Arrays.asList(new Object[] {1L, 8L, 27L, 64L, 125L}) ));
                add(new ArrowRelation("", Arrays.asList(new Object[] {1L, 16L, 81L, 256L, 625L}) ));
            }
        };

        var problems = new ArrayList<Object>();

        var metadata = Message.MetadataInfo.parseFrom(
                Files.readAllBytes(
                        Path.of(Paths.get(getClass().getResource("/metadata.pb").toURI()).toString())
                )
        );

        assertEquals(rsp.metadata.toString(), metadata.toString());
        assertEquals(rsp.results, results);
        assertEquals(rsp.problems, problems);
    }

    HttpClient getMockedHttpClient() throws IOException, InterruptedException, URISyntaxException {
        var httpRequest = HttpRequest.newBuilder()
                .headers("User-Agent", "rai-sdk-java")
                .uri(new URI("https://mocked/path/to/transactions")).build();
        var httpClient = Mockito.mock(HttpClient.class);
        var txnhttpResponse = Mockito.mock(HttpResponse.class);

        Mockito.when(txnhttpResponse.statusCode()).thenReturn(200);
        Mockito.when(txnhttpResponse.request()).thenReturn(httpRequest);
        Mockito.when(txnhttpResponse.headers()).thenReturn(
                HttpHeaders.of(new HashMap<>() {{put("Content-Type", new ArrayList<>() {{ add ("multipart/form-data; boundary=b11385ead6144ee0a9550db3672a7ccf"); }});
            }}, (k, v) -> true));
        Mockito.when(txnhttpResponse.body()).thenReturn(Files.readAllBytes(Path.of(Paths.get(getClass().getResource("/multipart.data").toURI()).toString())));

        Mockito.when(httpClient.send(any(), any())).thenReturn(txnhttpResponse);

        return httpClient;
    }
}
