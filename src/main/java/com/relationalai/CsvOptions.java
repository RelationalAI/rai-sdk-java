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

import java.util.Map;

public class CsvOptions {
    public Character delim;
    public Character escapeChar;
    public Integer headerRow;
    public Character quoteChar;
    public Map<String, String> schema;

    public CsvOptions() {}

    public CsvOptions withDelim(char delim) {
        this.delim = delim;
        return this;
    }

    public CsvOptions withEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }

    public CsvOptions withHeaderRow(int headerRow) {
        this.headerRow = headerRow;
        return this;
    }

    public CsvOptions withQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    public CsvOptions withSchema(Map<String, String> schema) {
        this.schema = schema;
        return this;
    }
}
