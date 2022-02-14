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

import java.util.HashMap;
import java.util.Map;

public class HttpError extends Exception {
    public int statusCode;
    public String message;

    static Map<Integer, String> statusText = new HashMap<Integer, String>() {
        {
            put(200, "OK");
            put(201, "Created");
            put(202, "Accepted");
            put(204, "No Content");
            put(400, "Bad Request");
            put(401, "Unauthorized");
            put(403, "Forbidden");
            put(404, "Not Found");
            put(405, "Method Not Allowed");
            put(409, "Conflict");
            put(410, "Gone");
            put(500, "Internal Server Error");
            put(501, "Not Implemented");
            put(502, "Bad Gateway");
            put(503, "Service Unavailable");
            put(504, "Gateway Timeout");
        }
    };

    public HttpError(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpError(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public String toString() {
        String result = Integer.toString(statusCode);
        if (statusText.containsKey(statusCode))
            result += " " + statusText.get(statusCode);
        if (message != null)
            result += "\n" + message;
        return result;
    }
}
