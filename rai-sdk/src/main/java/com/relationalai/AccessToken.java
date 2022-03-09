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

import java.time.Instant;
import com.jsoniter.annotation.JsonProperty;

public class AccessToken extends Entity {
    @JsonProperty(value = "access_token", required = true)
    public String token;

    @JsonProperty(value = "scope", required = true)
    public String scope;

    @JsonProperty(value = "expires_in", required = true)
    public int expiresIn; // total lifetime in seconds

    @JsonProperty(value = "created_on", required = false)
    public long createdOn; // epoch seconds

    // Instant that the token expires in epoch seconds.
    public long expiresOn() {
        return createdOn + expiresIn;
    }

    // Answers if the token is expired.
    public boolean isExpired() {
        var now = Instant.now().toEpochMilli() / 1000l;
        return now > expiresOn();
    }
}
