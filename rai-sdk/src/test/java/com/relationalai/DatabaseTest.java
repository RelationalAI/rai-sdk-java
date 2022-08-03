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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test the database management APIs.
@TestInstance(Lifecycle.PER_CLASS)
public class DatabaseTest extends UnitTest {
    Database find(Database[] databases, String name) {
        return find(databases, item -> item.name.equals(name));
    }

    //@Test
    void testDatabase() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureEngine(client);

        try {
            client.deleteDatabase(databaseName);
        } catch (HttpError e) {
            assertEquals(e.statusCode, 404);
        }

        var createRsp = client.createDatabase(databaseName, engineName, false);
        assertEquals(databaseName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        createRsp = client.createDatabase(databaseName, engineName, true); // overwrite
        assertEquals(databaseName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        var database = client.getDatabase(databaseName);
        assertEquals(databaseName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        var databases = client.listDatabases();
        database = find(databases, databaseName);
        assertNotNull(database);
        assertEquals(databaseName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        databases = client.listDatabases("CREATED");
        database = find(databases, databaseName);
        assertNotNull(database);
        assertEquals(databaseName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

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

    static final String testModel =
            "def R = \"hello\", \"world\"";

    static final String testJson = "{" +
            "\"name\":\"Amira\",\n" +
            "\"age\":32,\n" +
            "\"height\":null,\n" +
            "\"pets\":[\"dog\",\"rabbit\"]}";

    @Test void testDatabaseClone() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureEngine(client);

        try {
            client.deleteDatabase(databaseName);
        } catch (HttpError e) {
            assertEquals(e.statusCode, 404);
        }

        // Create a fresh database
        var createRsp = client.createDatabase(databaseName, engineName, false);
        assertEquals(databaseName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        // Load some data and a model
        var loadRsp = client.loadJson(databaseName, engineName, "test_data", testJson);
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        loadRsp = client.loadModel(databaseName, engineName, "test_model", testModel);
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        // Clone the database
        var databaseCloneName = databaseName + "-clone";
        createRsp = client.cloneDatabase(databaseCloneName, engineName, databaseName, true);
        assertEquals(databaseCloneName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        // Make sure the database exists
        var database = client.getDatabase(databaseCloneName);
        assertEquals(databaseCloneName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        var databases = client.listDatabases();
        database = find(databases, databaseCloneName);
        assertNotNull(database);
        assertEquals(databaseCloneName, createRsp.name);
        assertEquals("CREATED", createRsp.state);

        // Make sure the data was cloned 
        var rsp = client.executeV1(databaseCloneName, engineName, "test_data", true);

        Relation rel;

        rel = findRelation(rsp.output, ":name");
        assertNotNull(rel);

        rel = findRelation(rsp.output, ":age");
        assertNotNull(rel);

        rel = findRelation(rsp.output, ":height");
        assertNotNull(rel);

        rel = findRelation(rsp.output, ":pets");
        assertNotNull(rel);

        // Make sure the model was cloned
        var modelNames = client.listModelNames(databaseName, engineName);
        var name = find(modelNames, item -> item.equals("test_model"));
        assertNotNull(name);

        var models = client.listModels(databaseName, engineName);
        var model = find(models, m -> m.name.equals("test_model"));
        assertNotNull(model);

        model = client.getModel(databaseName, engineName, "test_model");
        assertNotNull(model);
        assertTrue(model.value.length() > 0);

        // Cleanup
        var deleteRsp = client.deleteDatabase(databaseCloneName);
        assertEquals(databaseCloneName, deleteRsp.name);
    }

    @AfterAll void tearDown() throws IOException, HttpError, InterruptedException {
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
