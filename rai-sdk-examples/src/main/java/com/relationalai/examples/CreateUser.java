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

package com.relationalai.examples;

import java.io.IOException;
import com.relationalai.Client;
import com.relationalai.Config;
import com.relationalai.HttpError;
import com.relationalai.Json;

public class CreateUser implements Runnable {
    String email, profile;
    String[] roles;

    public void parseArgs(String[] args) {
        var c = Command.create("CreateUser")
                .addArgument("email")
                .addOption("roles", String[].class, "user roles")
                .addOption("profile", "config profile (default: profile)")
                .parseArgs(args);
        this.email = c.getValue("email", String.class);
        this.roles = c.getValue("roles", String[].class);
        this.profile = c.getValue("profile", String.class);
    }

    public void run(String[] args) throws HttpError, InterruptedException, IOException {
        parseArgs(args);
        var cfg = Config.loadConfig("~/.rai/config", profile);
        var client = new Client(cfg);
        var rsp = client.createUser(email, roles);
        Json.print(rsp, 4);
    }
}
