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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test model APIs.
@TestInstance(Lifecycle.PER_CLASS)
public class ModelsTest extends UnitTest {
    static final Map<String, String> testModel = new HashMap<>(){{
        put("test_model", "def R = \"hello\", \"world\"");
    }};

    @Test void testModels() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var loadRsp = client.loadModels(databaseName, engineName, testModel);
        assertEquals("COMPLETED", loadRsp.transaction.state);
        assertEquals(0, loadRsp.problems.size());

        var model = client.getModel(databaseName, engineName, "test_model");
        assertEquals("test_model", model.name);
        assertEquals(testModel.get("test_model"), model.value);

        var modelNames = client.listModels(databaseName, engineName);
        var modelName = modelNames.stream().filter(item -> item.equals("test_model")).findFirst().orElse(null);
        assertNotNull(modelName);

        var deleteRsp = client.deleteModels(databaseName, engineName, new String[]{ "test_model" });
        assertEquals("COMPLETED", deleteRsp.transaction.state);
        assertEquals(0, deleteRsp.problems.size());

        HttpError error = null;
        try {
            client.getModel(databaseName, engineName, "test_model");
        } catch (HttpError e) {
            error = e;
        }
        assertTrue(error != null && error.statusCode == 404);

        modelNames = client.listModels(databaseName, engineName);
        modelName = modelNames.stream().filter(item -> item.equals("test_model")).findFirst().orElse(null);
        assertNull(modelName);
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
