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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The `Command` class provides a simplified interface to the commons-cli
 * command line parser and adds support for required, positional arguments
 */
public class Command {
    String appName;
    List<String> argNames;
    Options options;
    Map<String, Object> result;

    public Command(String appName) {
        this.appName = appName;
        this.result = new HashMap<String, Object>();
        this.argNames = new ArrayList<String>();
        this.options = new Options();
        this.options.addOption("?", "help", false, "display help");
    }

    public Command addFlag(String name, String description) {
        addOption(name, Boolean.class, 0, description);
        return this;
    }

    public Command addOption(Option opt) {
        options.addOption(opt);
        return this;
    }

    public Command addOption(String name, String description) {
        addOption(name, String.class, 1, description);
        return this;
    }

    public Command addOption(String name, Class<?> type, int numArgs, String description) {
        var option = new Option(name, description);
        option.setDescription(description);
        option.setLongOpt(name);
        option.setType(String.class);
        option.setArgs(numArgs);
        option.setValueSeparator(',');
        options.addOption(option);
        return this;
    }

    // Add a required positional argument with the given name. Positional args
    // will be parsed in the order they are added here.
    public Command addArgument(String name) {
        argNames.add(name);
        return this;
    }

    public static Command create(String appName) {
        return new Command(appName);
    }

    void errorMessage(String fmt, Object... args) {
        String msg;
        msg = String.format(fmt, args);
        msg = String.format("error: %s", msg);
        System.err.println(msg);
    }

    // Print an error message and exit.
    void error(String fmt, Object... args) {
        errorMessage(fmt, args);
        System.exit(1);
    }

    // Print an error message, usage string and exit.
    void errorUsage(String fmt, Object... args) {
        errorMessage(fmt, args);
        printUsage();
        System.exit(1);
    }

    void errorMissing(int count) {
        errorUsage("missing arguments: %s", missingArgs(count));
    }

    void errorTooMany(int count) {
        errorUsage("too many arguments");
    }

    public String getValue(String name) {
        return getValue(name, String.class);
    }

    public <T> T getValue(String name, Class<T> cls) {
        var v = result.get(name);
        var s = (String) v;
        if (cls == Boolean.class)
            v = s == null ? false : true;
        else if (cls == Character.class)
            v = s == null ? null : s.charAt(0);
        else if (cls == Integer.class)
            v = s == null ? null : Integer.parseInt(s);
        else if (cls == String[].class)
            v = s == null ? null : s.split(",");
        return cls.cast(v);
    }

    // Returns comma seperated list of missing argument names.
    String missingArgs(int count) {
        var required = argNames.size();
        assert count < required;
        var missing = new ArrayList<String>(required - count);
        for (var i = count; i < required; ++i)
            missing.add(argNames.get(i));
        return String.join(", ", missing);
    }

    // Parse command line arguments and collect both arguments and options
    // into a single results map.
    public Command parseArgs(String[] args) {
        CommandLine cmdline = null;
        var parser = new DefaultParser();
        try {
            cmdline = parser.parse(options, args);
        } catch (ParseException e) {
            error(e.toString()); // noreturn
        }
        args = cmdline.getArgs();
        if (args.length < argNames.size())
            errorMissing(args.length); // noreturn
        if (args.length > argNames.size())
            errorTooMany(args.length); //  noreturn
        for (var option : cmdline.getOptions()) {
            var name = option.getLongOpt();
            var numArgs = option.getArgs();
            Object value;
            switch (numArgs) {
                case 0:
                    assert option.getType() == Boolean.class;
                    value = "true";
                    break;
                case 1:
                    value = option.getValue();
                    break;
                default:
                    value = option.getValues();
                    break;
            }
            assert name != null;
            result.put(name, value);
        }
        for (var i = 0; i < argNames.size(); ++i) {
            var name = argNames.get(i);
            var value = args[i];
            result.put(name, value);
        }
        return this;
    }

    void printUsage() {
        String args = argNames.size() == 0 ? "" : String.join(" ", argNames);
        var msg = String.format("usage: %s [options] %s", appName, args);
        System.out.println(msg);
    }
}
