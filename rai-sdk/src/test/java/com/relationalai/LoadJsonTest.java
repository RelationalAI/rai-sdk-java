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
import java.io.IOException;

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
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        var rsp = client.executeV1(databaseName, engineName, "def output = sample");

        Relation rel;

        rel = findRelation(rsp.output, ":name");
        assertNotNull(rel);
        assertEquals(1, rel.columns.length);
        assertArrayEquals(new Object[][] {{"Amira"}}, rel.columns);

        rel = findRelation(rsp.output, ":age");
        assertNotNull(rel);
        assertEquals(1, rel.columns.length);
        assertArrayEquals(new Object[][] {{32.}}, rel.columns);

        rel = findRelation(rsp.output, ":height");
        assertNotNull(rel);
        assertEquals(1, rel.columns.length);
        assertArrayEquals(new Object[][] {{null}}, rel.columns);

        rel = findRelation(rsp.output, ":pets");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {{1., 2.}, {"dog", "rabbit"}}, rel.columns);
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
