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

package com.relationalai.models.oauthClient;

import com.jsoniter.annotation.JsonProperty;
import com.relationalai.models.Entity;

public class OAuthClient extends Entity {
    @JsonProperty(value = "id", required = true)
    public String id;

    @JsonProperty(value = "name", required = true)
    public String name;

    @JsonProperty(value = "account_name", required = true)
    public String accountName;

    @JsonProperty(value = "created_by", required = true)
    public String createdBy;

    @JsonProperty(value = "created_on", required = true)
    public String createdOn;

    public OAuthClient() {}
}
