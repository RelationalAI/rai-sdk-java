/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.relationalai;

import com.jsoniter.output.JsonStream;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionAsync extends Entity {

    String database;
    String engine;
    String command;
    Boolean readonly;
    Map<String, String> inputs;

    public TransactionAsync(
            String database, String engine, String command,
            Boolean readonly, Map<String, String> inputs) {
        this.database = database;
        this.engine = engine;
        this.command = command;
        this.readonly = readonly;
        this.inputs = inputs;
    }

    public TransactionAsync(String database, String engine, String command, Boolean readonly) {
        this.database = database;
        this.engine = engine;
        this.command = command;
        this.readonly = readonly;
        this.inputs = new HashMap<>();
    }

    // Construct the transaction payload and return serialized JSON string.
    String payload(DbAction... actions) {
        var data = new HashMap<String, Object>();
        data.put("dbname", database);
        data.put("readonly", readonly);
        data.put("engine_name", engine);
        data.put("query", command);

        var inputsList = new ArrayList<>();
        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            inputsList.add(
                    DbAction.makeQueryActionInput(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        data.put("inputs", inputsList);

        var output = new ByteArrayOutputStream();
        JsonStream.setIndentionStep(0);
        JsonStream.serialize(data, output);
        return output.toString();
    }

    // Returns the query params corresponding to the transaction state.
    QueryParams queryParams() {
        var result = new QueryParams();
        result.put("dbname", database);
        result.put("engine_name", engine);

        return result;
    }
}
