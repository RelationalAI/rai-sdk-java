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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Unit tests for the database management APIs.
@TestInstance(Lifecycle.PER_CLASS)
public class DatabaseTest extends UnitTest {
    Client client;

    void ensureDatabaseDeleted() throws InterruptedException, IOException {
        try {
            client.deleteDatabase(databaseName);
        } catch (HttpError e) {
            assertTrue(e.statusCode == 404);
        }
    }

    @BeforeAll void setup() throws IOException {
        var cfg = Config.loadConfig();
        this.client = new Client(cfg);
    }

    @Test void testCreateDatabase() throws HttpError, InterruptedException, IOException {
        ensureDatabaseDeleted();

        var createRsp = client.createDatabase(databaseName, engineName, true); // overwrite
        assertTrue(createRsp.name.equals(databaseName));
        assertTrue(createRsp.state.equals("CREATED"));

        var database = client.getDatabase(databaseName);
        assertTrue(database.name.equals(databaseName));
        assertTrue(database.state.equals("CREATED"));

        var databases = client.listDatabases();
        database = find(databases, item -> item.name.equals(databaseName));
        assertNotNull(database);

        databases = client.listDatabases("CREATED");
        database = find(databases, item -> item.name.equals(databaseName));
        assertNotNull(database);

        databases = client.listDatabases("FOOBAR");
        database = find(databases, item -> item.name.equals(databaseName));
        assertNull(database);

        var model = client.getModel(databaseName, engineName, "stdlib");
        assertNotNull(model);
        assertTrue(model.value.length() > 0);

        var modelNames = client.listModelNames(databaseName, engineName);
        var name = find(modelNames, item -> item.equals("stdlib"));
        assertNotNull(name);

        var models = client.listModels(databaseName, engineName);
        model = find(models, m -> m.name.equals("stdlib"));
        assertNotNull(model);

        var edbs = client.listEdbs(databaseName, engineName);
        var edb = find(edbs, item -> item.name.equals("rel"));
        assertNotNull(edb);

        var deleteRsp = client.deleteDatabase(databaseName);
        assertTrue(deleteRsp.name.equals(databaseName));
    }
}
