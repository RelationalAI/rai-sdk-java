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

class CreateEngineRequest extends Entity {
    @JsonProperty(value = "region", required = true)
    String region;

    @JsonProperty(value = "name", required = true)
    String name;

    @JsonProperty(value = "size", required = true)
    String size;

    CreateEngineRequest(String region, String name, String size) {
        this.region = region;
        this.name = name;
        this.size = size;
    }
}
