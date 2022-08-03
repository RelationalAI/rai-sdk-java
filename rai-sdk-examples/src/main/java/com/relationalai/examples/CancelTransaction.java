package com.relationalai.examples;

import com.relationalai.Client;
import com.relationalai.Config;
import com.relationalai.HttpError;
import com.relationalai.Json;

import java.io.IOException;

public class CancelTransaction implements Runnable {
    String id, profile;

    public void parseArgs(String[] args) {
        var c = Command.create("CancelTransaction")
                .addArgument("id")
                .addOption("profile", "config profile (default: default)")
                .parseArgs(args);
        this.id = c.getValue("id");
        this.profile = c.getValue("profile");
    }

    public void run(String[] args) throws HttpError, InterruptedException, IOException {
        parseArgs(args);
        var cfg = Config.loadConfig("~/.rai/config", profile);
        var client = new Client(cfg);

        var rsp = client.cancelTransaction(id);
        Json.print(rsp);
    }
}
