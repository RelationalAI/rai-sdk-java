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
import java.nio.file.Files;
import java.nio.file.Path;

import com.relationalai.Client;
import com.relationalai.Config;
import com.relationalai.HttpError;
import com.relationalai.Json;

public class ExecuteAsync implements Runnable {
    boolean readonly;
    String database, engine, command, filename, profile;

    // Returns the name of the file, without extension.
    static String readFile(String fname) throws IOException {
        return Files.readAllBytes(Path.of(fname)).toString();
    }

    String getCommand() throws IOException {
        if (command != null)
            return command; // prefer command line
        if (filename != null)
            return readFile(filename);
        return null;
    }

    public void parseArgs(String[] args) {
        var c = Command.create("ExecuteAsync")
                .addArgument("database")
                .addArgument("engine")
                .addOption("c", "rel source string")
                .addOption("f", "rel source file")
                .addFlag("readonly", "readonly query (default: false)")
                .parseArgs(args);
        this.database = c.getValue("database");
        this.engine = c.getValue("engine");
        this.command = c.getValue("c");
        this.filename = c.getValue("f");
        this.readonly = c.getValue("readonly", Boolean.class);
        this.profile = c.getValue("profile");
    }

    public void run(String[] args) throws HttpError, InterruptedException, IOException {
        parseArgs(args);
        var cfg = Config.loadConfig("~/.rai/config", profile);
        var client = new Client(cfg);
        String source = getCommand();
        if (source == null)
            return; // nothing to execute
        var rsp = client.executeAsync(database, engine, source, readonly);
        //Json.print(rsp);
        System.out.println(rsp);
    }
}
