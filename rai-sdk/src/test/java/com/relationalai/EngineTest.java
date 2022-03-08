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

package com.relationalai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test the engine management APIs.
@TestInstance(Lifecycle.PER_CLASS)
public class EngineTest extends UnitTest {
    Engine find(Engine[] engines, String name) {
        return find(engines, item -> item.name.equals(name));
    }

    @Test void testEngine() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        try {
            client.deleteEngineWait(engineName);
        } catch (HttpError e) {
            assertEquals(e.statusCode, 404);
        }

        var createRsp = client.createEngineWait(engineName);
        assertEquals(createRsp.name, engineName);
        assertEquals(createRsp.state, "PROVISIONED");

        var engine = client.getEngine(engineName);
        assertEquals(engine.name, engineName);
        assertEquals(engine.state, "PROVISIONED");

        var engines = client.listEngines();
        engine = find(engines, engineName);
        assertNotNull(engine);

        engines = client.listEngines("PROVISIONED");
        engine = find(engines, engineName);
        assertNotNull(engine);

        engines = client.listEngines("NONSENSE");
        engine = find(engines, engineName);
        assertNull(engine);

        HttpError error = null;

        try {
            // deleteEngineWait terminates its polling loop with a 404
            client.deleteEngineWait(engineName);
        } catch (HttpError e) {
            error = e;
        }
        assertTrue(error != null && error.statusCode == 404);

        try {
            client.getEngine(engineName);
        } catch (HttpError e) {
            error = e;
        }
        assertTrue(error != null && error.statusCode == 404);

        engines = client.listEngines();
        engine = find(engines, engineName);
        assertEquals(engine.state, "DELETED");
    }
}
