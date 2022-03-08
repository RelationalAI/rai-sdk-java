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

// Test the database management APIs.
@TestInstance(Lifecycle.PER_CLASS)
public class DatabaseTest extends UnitTest {
    Database find(Database[] databases, String name) {
        return find(databases, item -> item.name.equals(name));
    }

    @Test void runTests() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureEngine(client);

        try {
            client.deleteDatabase(databaseName);
        } catch (HttpError e) {
            assertEquals(e.statusCode, 404);
        }

        var createRsp = client.createDatabase(databaseName, engineName, false);
        assertTrue(createRsp.name.equals(databaseName));
        assertTrue(createRsp.state.equals("CREATED"));

        createRsp = client.createDatabase(databaseName, engineName, true); // overwrite
        assertTrue(createRsp.name.equals(databaseName));
        assertTrue(createRsp.state.equals("CREATED"));

        var database = client.getDatabase(databaseName);
        assertTrue(database.name.equals(databaseName));
        assertTrue(database.state.equals("CREATED"));

        var databases = client.listDatabases();
        database = find(databases, databaseName);
        assertNotNull(database);

        databases = client.listDatabases("CREATED");
        database = find(databases, databaseName);
        assertNotNull(database);

        databases = client.listDatabases("NONSENSE");
        database = find(databases, databaseName);
        assertNull(database);

        var edbs = client.listEdbs(databaseName, engineName);
        var edb = find(edbs, item -> item.name.equals("rel"));
        assertNotNull(edb);

        var modelNames = client.listModelNames(databaseName, engineName);
        var name = find(modelNames, item -> item.equals("stdlib"));
        assertNotNull(name);

        var models = client.listModels(databaseName, engineName);
        var model = find(models, m -> m.name.equals("stdlib"));
        assertNotNull(model);

        model = client.getModel(databaseName, engineName, "stdlib");
        assertNotNull(model);
        assertTrue(model.value.length() > 0);

        var deleteRsp = client.deleteDatabase(databaseName);
        assertTrue(deleteRsp.name.equals(databaseName));

        HttpError error = null;
        try {
            client.getDatabase(databaseName);
        } catch (HttpError e) {
            error = e;
        }
        assertTrue(error != null && error.statusCode == 404);

        databases = client.listDatabases();
        database = find(databases, databaseName);
        assertNull(database);
    }
}
