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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

// Test User APIs.
// TODO: Keep it disabled until the Auth0 API rate limiting issue is fixed.
@TestInstance(Lifecycle.PER_CLASS)
public class UserTest extends UnitTest {
    static UUID uuid = UUID.randomUUID();
    static String userEmail = String.format("java-sdk-%s@example.com", uuid);

    @Test
    void testUser() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        var rsp = client.findUser(userEmail);
        if (rsp != null) {
            client.deleteUser(rsp.id);
        }

        rsp = client.findUser(userEmail);
        assertNull(rsp);

        rsp = client.createUser(userEmail);
        assertEquals(userEmail, rsp.email);
        assertEquals("ACTIVE", rsp.status);
        assertArrayEquals(new String[] {"user"}, rsp.roles);

        var userId = rsp.id;

        var user = client.getUser(userId);
        assertNotNull(user);
        assertEquals(userId, user.id);
        assertEquals(userEmail, user.email);

        rsp = client.disableUser(userId);
        assertEquals(userId, user.id);
        assertEquals("INACTIVE", rsp.status);

        rsp = client.enableUser(userId);
        assertEquals(userId, user.id);
        assertEquals("ACTIVE", rsp.status);

        rsp = client.updateUser(userId, "INACTIVE");
        assertEquals(userId, user.id);
        assertEquals("INACTIVE", rsp.status);

        rsp = client.updateUser(userId, "ACTIVE");
        assertEquals(userId, user.id);
        assertEquals("ACTIVE", rsp.status);

        rsp = client.updateUser(userId, new String[] {"admin", "user"});
        assertEquals(userId, user.id);
        assertEquals("ACTIVE", rsp.status);
        assertArrayEquals(new String[] {"admin", "user"}, rsp.roles);

        rsp = client.updateUser(userId, "INACTIVE", new String[] {"user"});
        assertEquals(userId, user.id);
        assertEquals("INACTIVE", rsp.status);
        assertArrayEquals(new String[] {"user"}, rsp.roles);

        // Cleanup
        var deleteRsp = client.deleteUser(userId);
        assertEquals(userId, deleteRsp.id);

        try {
            client.getUser(userId);
        } catch (HttpError e) {
            assertEquals(404, e.statusCode);
        }
    }

    @AfterAll
    void tearDown() throws IOException, HttpError, InterruptedException {
        var client = createClient();
        var rsp = client.findUser(userEmail);
        if (rsp != null) {
            client.deleteUser(rsp.id);
        }
    }
}
