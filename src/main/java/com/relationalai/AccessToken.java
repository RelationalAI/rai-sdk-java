/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.relationalai;

import java.time.Instant;

import org.json.JSONObject;

public class AccessToken {
    public String token;
    public String scope;
    public int expiresIn; // total lifetime in seconds
    public long expiresOn; // expiration instant in epoch millis

    public Boolean isExpired() {
        return Instant.now().toEpochMilli() > expiresOn;
    }

    public static AccessToken fromJson(String s) {
        JSONObject data = new JSONObject(s);
        AccessToken result = new AccessToken();
        result.token = data.getString("access_token");
        result.scope = data.getString("scope");
        result.expiresIn = data.getInt("expires_in");
        result.expiresOn = Instant.now().toEpochMilli() + (result.expiresIn * 1000);
        return result;
    }
}
