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

import com.jsoniter.annotation.JsonProperty;

public class TransactionResult extends Model {
    @JsonProperty(value = "aborted", required = true)
    public Boolean aborted;

    @JsonProperty(value = "output", required = true)
    public Relation[] output;

    @JsonProperty(value = "problems", required = true)
    public Problem[] problems;

    public class Problem {
        @JsonProperty(value = "type", required = true)
        public String type;

        @JsonProperty(value = "error_code", required = true)
        public String errorCode;

        @JsonProperty(value = "is_error", required = true)
        public Boolean isError;

        @JsonProperty(value = "is_exception", required = true)
        public Boolean isException;

        @JsonProperty(value = "message", required = true)
        public String message;

        @JsonProperty(value = "report", required = true)
        public String report;
    }

    public class RelKey {
        @JsonProperty(value = "name", required = true)
        public String name;

        @JsonProperty(value = "keys", required = true)
        public String[] keys;

        @JsonProperty(value = "values", required = true)
        public String[] values;
    }

    public class Relation {
        @JsonProperty(value = "rel_key", required = true)
        public RelKey relKey;

        @JsonProperty(value = "columns", required = true)
        public Object[][] columns;
    }
}
