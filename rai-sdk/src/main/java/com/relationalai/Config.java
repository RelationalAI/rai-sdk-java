/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.relationalai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Config {
    public String region;
    public String scheme;
    public String host;
    public String port;
    public Credentials credentials = null;

    public static String DEFAULT_FILENAME = "~/.rai/config";
    public static String DEFAULT_PROFILE = "default";

    public Config() {}

    private static Credentials getCredentials(Properties data) {
        String clientId = data.getProperty("client_id", null);
        if (clientId != null) {
            String clientSecret = data.getProperty("client_secret", null);
            String clientCredentialsUrl = data.getProperty("client_credentials_url", null);
            if (clientSecret != null)
                return new ClientCredentials(clientId, clientSecret, clientCredentialsUrl);
        }
        return null;
    }

    public void load(InputStream input, String profile) throws IOException {
        Properties data = read(input, profile);
        this.region = data.getProperty("region", null);
        this.scheme = data.getProperty("scheme", null);
        this.host = data.getProperty("host", null);
        this.port = data.getProperty("port", null);
        this.credentials = getCredentials(data);
    }

    public static Config loadConfig() throws IOException {
        return loadConfig(DEFAULT_FILENAME, DEFAULT_PROFILE);
    }

    public static Config loadConfig(InputStream input) throws IOException {
        return loadConfig(input, DEFAULT_PROFILE);
    }

    public static Config loadConfig(String fileName) throws IOException {
        return loadConfig(fileName, DEFAULT_PROFILE);
    }

    public static Config loadConfig(String fileName, String profile) throws IOException {
        if (fileName == null)
            fileName = DEFAULT_FILENAME;
        if (profile == null)
            profile = DEFAULT_PROFILE;
        if (fileName.startsWith("~" + File.separator))
            fileName = System.getProperty("user.home") + fileName.substring(1);
        FileInputStream input = new FileInputStream(fileName);
        return loadConfig(input, profile);
    }

    public static Config loadConfig(InputStream input, String profile) throws IOException {
        assert profile != null;
        Config cfg = new Config();
        cfg.load(input, profile);
        return cfg;
    }

    //
    // Config file reader
    //

    // Answers if the given line is a stanza header and if so return the stana
    // name, othewise null.
    static String maybeStanza(String line) {
        if (line.startsWith("[") && line.endsWith("]"))
            return line.substring(1, line.length() - 1);
        return null;
    }

    // Strip comments, preceeding and trailing whitespace.
    static String stripExtra(String line) {
        int index = line.indexOf('#');
        if (index != -1)
            line = line.substring(0, index);
        return line.strip();
    }

    // Read config settings from the given `InputStream`, returning the named
    // profile (aka, stanza) as a Java `Properties` object.
    static Properties read(InputStream input, String profile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            line = stripExtra(line);
            if (line.isEmpty())
                continue;
            String stanza = maybeStanza(line);
            if (stanza != null && stanza.equals(profile)) {
                return readStanza(reader);
            }
        }
        return null;
    }

    // Read config settings from the current stanza, stopping when we find a
    // new stanza, or end of file. Returns results as a Java `Properties` object.
    static Properties readStanza(BufferedReader reader) throws IOException {
        Properties data = new Properties();
        String line;
        while ((line = reader.readLine()) != null) {
            line = stripExtra(line);
            if (line.isEmpty())
                continue;
            if (maybeStanza(line) != null) // new stanza
                break;
            int equals = line.indexOf("=");
            if (equals == -1)
                continue;
            String key = line.substring(0, equals).strip();
            String value = line.substring(equals + 1).strip();
            data.setProperty(key, value);
        }
        return data;
    }
}
