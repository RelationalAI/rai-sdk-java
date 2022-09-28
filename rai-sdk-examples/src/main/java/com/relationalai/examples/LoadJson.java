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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.relationalai.Client;
import com.relationalai.utils.Config;
import com.relationalai.errors.HttpError;
import com.relationalai.utils.Json;

public class LoadJson implements Runnable {
    String database, engine, filename, relation, profile;

    // Returns the name of the file, without extension.
    static String sansext(String fname) {
        var file = new File(fname);
        var name = file.getName();
        var dot = name.lastIndexOf('.');
        if (dot > 0)
            name = name.substring(0, dot);
        return name;
    }

    public void parseArgs(String[] args) {
        var c = Command.create("LoadJson")
                .addArgument("database")
                .addArgument("engine")
                .addArgument("file")
                .addOption("r", "relation name (default: file name)")
                .addOption("profile", "config profile (default: default)")
                .parseArgs(args);
        this.database = c.getValue("database");
        this.engine = c.getValue("engine");
        this.filename = c.getValue("file");
        this.relation = c.getValue("r");
        this.profile = c.getValue("profile");
    }

    public void run(String[] args) throws HttpError, InterruptedException, IOException {
        parseArgs(args);
        var cfg = Config.loadConfig("~/.rai/config", profile);
        var client = new Client(cfg);
        var input = new FileInputStream(filename);
        var rname = relation != null ? relation : sansext(filename);
        var rsp = client.loadJson(database, engine, rname, input);
        Json.print(rsp, 4);
    }
}
