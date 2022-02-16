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
    boolean abort;
    boolean readonly;
    boolean noWaitDurable;
    int version;

    Transaction(String region, String database, String engine, String mode) {
        this.region = region;
        this.database = database;
        this.engine = engine;
        this.mode = mode;
        this.readonly = false;
    }

    Transaction(
            String region, String database, String engine,
            String mode, boolean readonly) {
        this.region = region;
        this.database = database;
        this.engine = engine;
        this.mode = mode;
        this.readonly = readonly;
    }

    Transaction(
            String region, String database, String engine,
            String mode, boolean readonly, String source) {
        this.region = region;
        this.database = database;
        this.engine = engine;
        this.mode = mode;
        this.readonly = readonly;
        this.source = source;
    }

    // Construct the transaction payload and return serialized JSON string.
    String payload(DbAction... actions) {
        var data = new HashMap<String, Object>();
        data.put("type", "Transaction");
        data.put("mode", mode != null ? mode : "OPEN");
        data.put("dbname", database);
        data.put("abort", abort);
        data.put("nowait_durable", noWaitDurable);
        data.put("readonly", readonly);
        data.put("version", version);
        data.put("actions", DbAction.makeActions(actions));
        if (engine != null)
            data.put("computeName", engine);
        if (source != null)
            data.put("source_dbname", source);
        var output = new ByteArrayOutputStream();
        JsonStream.setIndentionStep(0);
        JsonStream.serialize(data, output);
        return output.toString();
    }

    // Returns the query params corresponding to the transaction state.
    QueryParams queryParams() {
        var result = new QueryParams();
        result.put("region", region);
        result.put("dbname", database);
        result.put("compute_name", engine);
        result.put("open_mode", mode != null ? mode : "OPEN");
        if (source != null)
            result.put("source_dbname", source);
        return result;
    }
}
