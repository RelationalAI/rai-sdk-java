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

import java.util.HashMap;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * The `Command` class provides a simplified interface to the commons-cli
 * command line paresr that adds support for required, positional arguments
 */
public class Command {
    String appName;
    List<String> argNames;
    Options options;
    HashMap<String, Object> results;

    // name, type, hasArg, required, description

    public Command(String appName) {
        this.appName = appName;
        this.result = null;
        this.argNames = new List<String>();
        this.options = new Options();
        this.options.addOption("?", "help", false, "display help");
    }

    public Command addOption(Option opt) {
        this.options.addOption(opt);
        return this;
    }

    public Command addOption(String opt, String description) {
        addOption(opt, String.class, 1, description);
        return this;
    }

    public Command addOption(String name, Class type, String description) {
        var numArgs = type == Boolean ? 0 : 1;
        addOption(name, type, numArgs, description);
        return this;
    }

    public Command addOption(String name, Class type, int numArgs, String description) {
        var option = new Option();
        option.setArgName(name);
        option.setType(type);
        option.setArgs(numArgs);
        option.setDescription(description);
        this.options.addOption(option);
        return this;
    }

    // Add a required positional argument with the given name. Positional args
    // will be parsed in the order they are added here.
    public Command addArg(String name) {
        this.argNames.add(name);
        return this;
    }

    public static Command create(String appName) {
        return new Command(appName);
    }

    void error(String fmt, Object... args) {
        Strin msg;
        msg = String.format(fmt, args);
        msg = String.format("error: %s\n", msg);
        System.err.println(msg);
    }

    // Report missing arguments, print usage and exit.
    void errorMissing(int count) {
        error("missing arguments: %s", missingArgs(count));
        printUsage();
        System.exit(1);
    }

    public <T> T getValue(String name) {
        var v = this.get(name);
        if (T instanceof String)
            return v.toString();
        if (T instanceof Integer)
            return Integer.parseInt(v);
        if (T instanceof Boolean)
            return Boolean.parseBoolean(v);
        throw new RuntimeException("bad option type");
    }

    // Returns comma seperated list of missing argument names.
    String missingArgs(int count) {
        assert count < argNames.length;
        var missing = new List<String>(argsNames.length - count);
        for (var i = count; i < argNames.length; ++i)
            missing.add(argNames.get(i));
        return String.join(", ", missing);
    }

    public Command parseArgs(String[] args) {
        var parser = new DefaultParser();
        cmdline = parser.parse(options, args);
        args = cmdline.getArgs();
        if (args.length < argNames.size())
            errorMissing(args.length); // noreturn
        // combine options and positional argumets into command map.
        for (var i = 0; i < argNames.size(); ++i) {
            var name = argNames.get(i);
            var value = args[i];
            argMap.put(name, value);
        }
    }

    void printUsage() {
        var names = String.join(", ", argNames);
        var msg = String.format("usage: %s [options] ", appName, names);
    }
}
