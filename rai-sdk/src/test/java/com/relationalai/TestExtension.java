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

package com.relationalai;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!started) {
            started = true;
            Client client = UnitTest.createClient();
            Engine engine = client.createEngineWait(UnitTest.engineName);
            assertEquals(engine.name, UnitTest.engineName);
            assertEquals(engine.state, "PROVISIONED");
            context.getRoot().getStore(GLOBAL).put("com.relationalai.test", this);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        Client client = UnitTest.createClient();
        HttpError error = null;
        try {
            System.out.println(">>>>>>>>>> Deleting the engine: " + UnitTest.engineName);
            client.deleteEngineWait(UnitTest.engineName);
        } catch (HttpError e) {
            error = e;
        }
        assertTrue(error != null && error.statusCode == 404);
    }
}
