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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExecuteAsyncTest extends UnitTest {
    @Test
    void testExecuteAsync() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var query = "x, x^2, x^3, x^4 from x in {1; 2; 3; 4; 5}";

        var rsp = client.executeAsyncWait(databaseName, engineName, query, true);
        assertEquals(
                rsp.get("results").toString(),
                "[{\"v1\":\"[1, 2, 3, 4, 5]\",\"v2\":\"[1, 4, 9, 16, 25]\",\"v3\":\"[1, 8, 27, 64, 125]\",\"v4\":\"[1, 16, 81, 256, 625]\"}]"
        );
        assertEquals(
                rsp.get("metadata").toString(),
                "[{\"relationId\":\"/:output/Int64/Int64/Int64/Int64\",\"types\":[\":output\",\"Int64\",\"Int64\",\"Int64\",\"Int64\"]}]"
        );
        assertEquals(
                rsp.get("problems").toString(),
                "[]"
        );
    }

    @AfterAll
    void tearDown() throws IOException, HttpError, InterruptedException {
        var client = createClient();
        var deleteRsp = client.deleteDatabase(databaseName);
        assertEquals(databaseName, deleteRsp.name);
        try {
            // deleteEngineWait terminates its polling loop with a 404
            client.deleteEngineWait(engineName);
        } catch (HttpError e) {
            assertEquals(e.statusCode, 404);
        }
    }
}
