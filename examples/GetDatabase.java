/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express
 * or implied. See the License for the specific language governing permissions
 * and limitations under
 * the License.
 */

import com.relationalai.Client;
import com.relationalai.Config;
import com.relationalai.Json;

public class GetDatabase {
    public static void main(String[] args) {
        try {
            var cfg = Config.loadConfig();
            var client = new Client(cfg);
            var rsp = client.getDatabase("sdk-test");
            Json.print(rsp, 4);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
