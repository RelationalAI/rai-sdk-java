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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import com.jsoniter.output.JsonStream;

// Represents the state required to dispatch a transacation. This is an 
// internal helper object used to for the construction of transaction payloads
// and query params.
class Transaction {
    String region;
    String database;
    String engine;
    String mode;
    String source;
    Boolean abort;
    Boolean readonly;
    Boolean noWaitDurable;
    int version;

    Transaction(String region, String database, String engine, String mode) {
        this.region = region;
        this.database = database;
        this.engine = engine;
        this.mode = mode;
    }

    Transaction(String region, String database, String engine, String mode, Boolean readonly) {
        this.region = region;
        this.database = database;
        this.engine = engine;
        this.mode = mode;
        this.readonly = readonly;
    }

    // Construct the transaction payload and return serialized JSON string.
    String payload(DbAction... actions) {
        var data = new HashMap<String, Object>() {
            {
                put("type", "Transaction");
                put("mode", mode != null ? mode : "OPEN");
                put("dbname", database);
                put("abort", abort);
                put("nowait_durable", noWaitDurable);
                put("readonly", readonly);
                put("version", version);
                put("actions", DbAction.makeActions(actions));
            }
        };
        if (engine != null)
            data.put("computeName", engine);
        if (source != null)
            data.put("source_dbname", source);
        var output = new ByteArrayOutputStream();
        JsonStream.setIndentionStep(0);
        JsonStream.serialize(this, output);
        return output.toString();
    }

    // Returns the query params corresponding to the transaction state.
    QueryParams queryParams() {
        var result = new QueryParams() {
            {
                put("region", region);
                put("dbname", database);
                put("compute_name", engine);
                put("open_mode", mode != null ? mode : "OPEN");
            }
        };
        if (source != null)
            result.put("source_dbname", source);
        return result;
    }
}
