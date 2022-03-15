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

import java.io.IOException;
import java.util.function.Predicate;

public abstract class UnitTest {
    static String databaseName = "sdk-test";
    static String engineName = "sdk-test";

    // Returns a new client object constructed from default config settings.
    Client createClient() throws IOException {
        var cfg = Config.loadConfig();
        return new Client(cfg);
    }

    // Ensure that the test database exists.
    void ensureDatabase() throws HttpError, InterruptedException, IOException {
        ensureDatabase(createClient());
    }

    void ensureDatabase(Client client) throws HttpError, InterruptedException, IOException {
        ensureEngine(client);
        client.createDatabase(databaseName, engineName, true); // overwrite
    }

    void ensureEngine() throws HttpError, InterruptedException, IOException {
        ensureEngine(createClient());
    }

    // Ensure that the test engine exists.
    void ensureEngine(Client client) throws HttpError, InterruptedException, IOException {
        try {
            client.getEngine(engineName);
        } catch (HttpError e) {
            assert (e.statusCode == 404);
            client.createEngineWait(engineName);
        }
    }

    // Return the item in the given array that satisfies the given predicate.
    static <T> T find(T[] items, Predicate<T> predicate) {
        for (var item : items) {
            if (predicate.test(item))
                return item;
        }
        return null;
    }

    static Relation findRelation(Relation[] relations, String colName) {
        for (var relation : relations) {
            var keys = relation.relKey.keys;
            if (keys.length == 0)
                continue;
            var name = keys[0];
            if (name.equals(colName))
                return relation;
        }
        return null;
    }
}
