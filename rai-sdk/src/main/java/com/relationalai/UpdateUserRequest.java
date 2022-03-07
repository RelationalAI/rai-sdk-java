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

public class UpdateUserRequest extends Entity {
    @JsonProperty(value = "status", required = false, defaultValueToOmit = "null")
    public String status;

    @JsonProperty(value = "roles", required = true)
    public String[] roles;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String status) {
        this.status = status;
        this.roles = new String[] {};
    }

    public UpdateUserRequest(String[] roles) {
        this.roles = roles;
    }

    public UpdateUserRequest(String status, String[] roles) {
        this.status = status;
        if (roles == null)
            roles = new String[] {};
        this.roles = roles;
    }
}
