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

package com.relationalai.credentials;

public class ClientCredentials implements Credentials {
    public static String DEFAULT_CLIENT_CREDENTIALS_URL =
        "https://login.relationalai.com/oauth/token";

    public AccessToken accessToken; // cached access token
    public String clientId;
    public String clientSecret;
    public String clientCredentialsUrl;

    public ClientCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientCredentialsUrl = DEFAULT_CLIENT_CREDENTIALS_URL;
    }

    public ClientCredentials(String clientId, String clientSecret, String clientCredentialsUrl) {
        if (clientCredentialsUrl == null)
            clientCredentialsUrl = DEFAULT_CLIENT_CREDENTIALS_URL;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientCredentialsUrl = clientCredentialsUrl;
    }
}
