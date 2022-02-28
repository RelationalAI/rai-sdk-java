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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

// Represents a collection of query paramters. The values may be String,
// String[], int, Boolean or null.
public class QueryParams extends HashMap<String, Object> {
    // Returns a query string encoded in the format the RAI REST API expects.
    public String encode() {
        StringBuilder result = new StringBuilder();
        for (var entry : this.entrySet()) {
            var k = entry.getKey();
            var v = entry.getValue();
            append(result, k, v);
        }
        return result.toString();
    }

    // Encode the given k, v pair, and append to the given builder.
    private static void append(StringBuilder builder, String k, Object v) {
        if (v instanceof String[]) {
            for (var vv : (String[]) v)
                append(builder, k, vv);
            return;
        }
        String value;
        if (v instanceof String) {
            value = (String) v;
        } else if (v instanceof Integer) {
            value = Integer.toString((int) v);
        } else if (v instanceof Boolean) {
            value = Boolean.toString((boolean) v);
        } else {
            throw new IllegalArgumentException("param value is an invalid type");
        }
        if (builder.length() > 0)
            builder.append('&');
        builder.append(encodeValue(k));
        builder.append('=');
        builder.append(encodeValue(value));
    }

    // Encode an element of a query parameter.
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
