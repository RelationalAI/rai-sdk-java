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
import org.junit.jupiter.api.Disabled;
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

        var rsp = client.findOAuthClient(clientName);
        if (rsp != null) {
            client.deleteOAuthClient(rsp.id);
        }

        rsp = client.findOAuthClient(clientName);
        assertNull(rsp);

        rsp = client.createOAuthClient(clientName);
        assertEquals(clientName, rsp.name);

        var clientId = rsp.id;

        rsp = client.findOAuthClient(clientName);
        assertNotNull(rsp);
        assertEquals(clientId, rsp.id);
        assertEquals(clientName, rsp.name);

        System.out.println("==> getOAuthClient");
        rsp = client.getOAuthClient(clientId);
        assertNotNull(rsp);
        assertEquals(clientId, rsp.id);
        assertEquals(clientName, rsp.name);

        var clients = client.listOAuthClients();
        var found = find(clients, item -> item.id.equals(clientId));
        assertNotNull(found);
        assertEquals(clientId, found.id);
        assertEquals(clientName, found.name);

        var deleteRsp = client.deleteOAuthClient(clientId);
        assertEquals(clientId, deleteRsp.id);

        rsp = client.findOAuthClient(clientName);
        assertNull(rsp);
    }

    @AfterAll
    void tearDown() throws IOException, HttpError, InterruptedException {
        var client = createClient();
        var rsp = client.findOAuthClient(clientName);
        if (rsp != null) {
            client.deleteOAuthClient(rsp.id);
        }
    }
}
