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

package com.relationalai.models.database;

import com.jsoniter.annotation.JsonProperty;
import com.relationalai.models.Entity;

public class Database extends Entity {
    @JsonProperty(value = "id", required = true)
    public String id;

    @JsonProperty(value = "name", required = true)
    public String name;

    @JsonProperty(value = "region", required = true)
    public String region;

    @JsonProperty(value = "account_name", required = true)
    public String accountName;

    @JsonProperty(value = "created_by", required = true)
    public String createdBy;

    @JsonProperty(value = "deleted_on", required = false)
    public String deletedOn;

    @JsonProperty(value = "deleted_by", required = false)
    public String deletedBy;

    @JsonProperty(value = "default_compute_name", required = false)
    public String defaultEngine;

    @JsonProperty(value = "state", required = true)
    public String state;

    public Database() {}
}
