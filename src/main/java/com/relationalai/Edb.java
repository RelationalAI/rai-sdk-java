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

import java.util.List;
import org.json.JSONObject;

public class Edb extends Model {
    public String name;
    public List<String> keys;
    public List<String> values;

    Edb() {}

    Edb(JSONObject data) {
        init(data);
    }

    public void init(JSONObject data) {
        name = data.getString("name");
        keys = asStringList(data, "keys");
        values = asStringList(data, "values");
    }

    public static Edb fromJson(String data) {
        return new Edb(new JSONObject(data));
    }
}
