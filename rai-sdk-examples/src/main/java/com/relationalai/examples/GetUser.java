/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.relationalai.examples;

import java.io.IOException;
import com.relationalai.Client;
import com.relationalai.Config;
import com.relationalai.HttpError;
import com.relationalai.Json;

// Returns the user with the given id.
public class GetUser implements Runnable {
    String id, profile;

    public void parseArgs(String[] args) {
        var c = Command.create("GetUser")
                .addArgument("id")
                .addOption("profile", "config profile (default: default)")
                .parseArgs(args);
        this.id = c.getValue("id");
        this.profile = c.getValue("profile");
    }

    public void run(String[] args) throws HttpError, InterruptedException, IOException {
        parseArgs(args);
        var cfg = Config.loadConfig(DEFAULT_CONFIG_PATH, profile);
        var client = new Client(cfg);
        var rsp = client.getUser(id);
        Json.print(rsp, 4);
    }
}
