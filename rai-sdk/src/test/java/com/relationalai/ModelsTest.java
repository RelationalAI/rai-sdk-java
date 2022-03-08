/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express
 * or implied. See the License for the specific language governing permissions
 * and limitations under
 * the License.
 */

package com.relationalai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test the models APIs.
@TestInstance(Lifecycle.PER_CLASS)
public class ModelsTest extends UnitTest {
    @Test void runTests() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var source = "def R = \"hello\", \"world\"";
        var loadRsp = client.loadModel(databaseName, engineName, "hello", source);
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        var model = client.getModel(databaseName, engineName, "hello");
        assertEquals("hello", model.name);

        var modelNames = client.listModelNames(databaseName, engineName);
        var modelName = find(modelNames, item -> item.equals("hello"));
        assertNotNull(modelName);

        var models = client.listModels(databaseName, engineName);
        model = find(models, item -> item.name.equals("hello"));
        assertNotNull(model);

        var deleteRsp = client.deleteModel(databaseName, engineName, "hello");
        assertEquals(false, deleteRsp.aborted);
        assertEquals(0, deleteRsp.output.length);
        assertEquals(0, deleteRsp.problems.length);

        HttpError error = null;
        try {
            client.getModel(databaseName, engineName, "hello");
        } catch (HttpError e) {
            error = e;
        }
        assertTrue(error != null && error.statusCode == 404);

        modelNames = client.listModelNames(databaseName, engineName);
        modelName = find(modelNames, item -> item.equals("hello"));
        assertNull(modelName);

        models = client.listModels(databaseName, engineName);
        model = find(models, item -> item.name.equals("hello"));
        assertNull(model);
    }
}
