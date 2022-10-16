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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.arrow.vector.util.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test loading JSON data.
@TestInstance(Lifecycle.PER_CLASS)
public class LoadJsonTest extends UnitTest {
    static final String sample = "{" +
            "\"name\":\"Amira\",\n" +
            "\"age\":32,\n" +
            "\"height\":null,\n" +
            "\"pets\":[\"dog\",\"rabbit\"]}";

    @Test void testLoadJson() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var loadRsp = client.loadJson(databaseName, engineName, "sample", sample);
        assertEquals("COMPLETED", loadRsp.transaction.state);
        assertEquals(0, loadRsp.problems.size());

        var rsp = client.execute(databaseName, engineName, "def output = sample");
        assertEquals(new ArrayList<Object>() {{ add(32L); }}, rsp.results.get(0).table);
        assertEquals(new ArrayList<Object>() {{ add(new HashMap<String, Object>()); }}, rsp.results.get(1).table);
        assertEquals(new ArrayList<Object> (){{ add(new Text("Amira")); }}, rsp.results.get(2).table);
        assertEquals(new ArrayList<Object>(){{ add(1L); add(2L);}}, rsp.results.get(3).table);
        assertEquals(new ArrayList<Object>(){{ add(new Text("dog")); add(new Text("rabbit"));}}, rsp.results.get(4).table);
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
