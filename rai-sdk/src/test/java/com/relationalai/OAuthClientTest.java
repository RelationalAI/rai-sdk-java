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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test OAuth Client APIs
@TestInstance(Lifecycle.PER_CLASS)
public class OAuthClientTest extends UnitTest {
    static UUID uuid = UUID.randomUUID();
    static String clientName = String.format("sdk-test-client-%s", uuid);

    @Test void testOAuthClient() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        log.info("client name: " + clientName);

        var rsp = client.findOAuthClient(clientName);
        if (rsp != null) {
            client.deleteOAuthClient(rsp.id);
        }

        rsp = client.createOAuthClient(clientName);
        assertEquals(clientName, rsp.name);

        var clientId = rsp.id;

        rsp = client.getOAuthClient(clientId);
        assertNotNull(rsp);
        assertEquals(clientId, rsp.id);
        assertEquals(clientName, rsp.name);

        var deleteRsp = client.deleteOAuthClient(clientId);
        assertEquals(clientId, deleteRsp.id);

        try {
            client.getOAuthClient(clientId);
        } catch (HttpError e) {
            assertEquals(404, e.statusCode);
        }
    }

    @AfterAll
    void tearDown() throws IOException, InterruptedException {
        var client = createClient();
        try {
            var rsp = client.findOAuthClient(clientName);
            if (rsp != null) {
                client.deleteOAuthClient(rsp.id);
            }
        } catch (HttpError e) {
            System.out.println(e.toString());
        }
    }
}
