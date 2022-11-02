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
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

// This handler caches tokens in ~/.rai/tokens.json. It will attempt to load
// a token from the cache file and if it is not found or has expired, it will
// delegate to client.GetAccessToken to retrieve a new token and will save it
// in the cache file.
public class DefaultAccessTokenHandler implements AccessTokenHandler {
    // Returns the name of the token cache file.
    String cacheName() {
        var home = System.getProperty("user.home");
        return home + File.separator + ".rai" + File.separator + "tokens.json";
    }

    public AccessToken getAccessToken(Client client, ClientCredentials credentials)
            throws HttpError, InterruptedException, IOException {
        var token = readAccessToken(credentials);
        if (token != null && !token.isExpired()) {
            System.out.println("===> using cached access token");
            return token;
        }

        token = client.fetchAccessToken(credentials);
        System.out.println("===> generating a new token");
        if (token != null)
            writeAccessToken(credentials, token);

        return token;
    }

    AccessToken readAccessToken(ClientCredentials credentials) {
        var cache = readTokenCache();
        if (cache == null)
            return null;
        var item = cache.get(credentials.clientId);
        var valueType = item.valueType();
        if (valueType == ValueType.INVALID) // not found
            return null;
        assert valueType == ValueType.OBJECT; // validated by readTokenCache
        return item.bindTo(new AccessToken());
    }

    // Attempt to read and deserialize the token cache file.
    Any readTokenCache() {
        try (var input = new FileInputStream(cacheName())) {
            try {
                var data = new String(input.readAllBytes());
                var cache = Json.deserialize(data);
                if (cache.valueType() != ValueType.OBJECT)
                    return null; // cache file corrupt, ignore
                return cache;
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
