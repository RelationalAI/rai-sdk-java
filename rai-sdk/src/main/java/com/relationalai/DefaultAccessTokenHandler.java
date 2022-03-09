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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.jsoniter.any.Any;

// Attempts to load cached token from ~/.rai/tokens.json, and if not found or
// the token is expired, will delegate to client.fetchAccessToken to retrieve
// a new token and will save in the tokens file.
public class DefaultAccessTokenHandler implements AccessTokenHandler {
    String cacheName() {
        var home = System.getProperty("user.home");
        return home + File.separator + ".rai" + File.separator + "tokens.json";
    }

    AccessToken fetchToken(Client client, ClientCredentials credentials) {
        AccessToken token = null;
        try {
            token = client.fetchAccessToken(credentials);
        } catch (HttpError e) {} catch (InterruptedException e) {} catch (IOException e) {}
        return token;
    }

    public AccessToken getAccessToken(Client client, ClientCredentials credentials) {
        var token = readAccessToken(credentials);
        if (token != null && !token.isExpired())
            return token;

        token = fetchToken(client, credentials);
        if (token != null)
            writeAccessToken(credentials, token);

        return token;
    }

    AccessToken readAccessToken(ClientCredentials credentials) {
        var cache = readTokenCache();
        if (cache == null)
            return null;
        var item = cache.get(credentials.clientId);
        if (item == null)
            return null;
        return item.bindTo(new AccessToken());
    }

    Any readTokenCache() {
        try (var input = new FileInputStream(cacheName())) {
            try {
                var data = new String(input.readAllBytes());
                return Json.deserialize(data);
            } catch (IOException e) {}
        } catch (IOException e) {}
        return null;
    }

    void writeAccessToken(ClientCredentials credentials, AccessToken token) {
        var cache = readTokenCache();
        var map = cache == null ? new HashMap<String, Any>() : cache.asMap();
        map.put(credentials.clientId, Any.wrap(token));
        writeTokenCache(map);
    }

    void writeTokenCache(Map<String, Any> cache) {
        try (var output = new FileOutputStream(cacheName())) {
            Json.serialize(cache, output);
        } catch (IOException e) {}
    }
}
