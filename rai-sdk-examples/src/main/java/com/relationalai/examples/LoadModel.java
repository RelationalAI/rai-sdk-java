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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.relationalai.Client;
import com.relationalai.Config;
import com.relationalai.HttpError;
import com.relationalai.Json;

public class LoadModel implements Runnable {
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

    // todo: schema
    public void parseArgs(String[] args) {
        var c = Command.create("LoadModel")
                .addArgument("database")
                .addArgument("engine")
                .addArgument("file")
                .parseArgs(args);
        this.database = c.getValue("database");
        this.engine = c.getValue("engine");
        this.filename = c.getValue("file");
        this.profile = c.getValue("profile");
    }

    public void run(String[] args) throws HttpError, InterruptedException, IOException {
        parseArgs(args);
        var cfg = Config.loadConfig("~/.rai/config", profile);
        var client = new Client(cfg);
        var name = sansext(filename);
        var input = new FileInputStream(filename);
        var rsp = client.loadModel(database, engine, name, input);
        Json.print(rsp, 4);
    }
}
