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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.arrow.vector.util.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

// Test loading CSV data.
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith({TestExtension.class})
public class LoadCsvTest extends UnitTest {
    static final String sample = "" +
            "cocktail,quantity,price,date\n" +
            "\"martini\",2,12.50,\"2020-01-01\"\n" +
            "\"sazerac\",4,14.25,\"2020-02-02\"\n" +
            "\"cosmopolitan\",4,11.00,\"2020-03-03\"\n" +
            "\"bellini\",3,12.25,\"2020-04-04\"\n";

    @Test void testLoadCsv() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var loadRsp = client.loadCsv(databaseName, engineName, "sample", sample);
        assertEquals("COMPLETED", loadRsp.transaction.state);
        assertEquals(0, loadRsp.problems.size());

        var rsp = client.execute(databaseName, engineName, "def output = sample");
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(0).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("martini"));
                         add(new Text("sazerac"));
                         add(new Text("cosmopolitan"));
                         add(new Text("bellini")); }},
                rsp.results.get(1).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(2).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("2020-01-01"));
                         add(new Text("2020-02-02"));
                         add(new Text("2020-03-03"));
                         add(new Text("2020-04-04")); }},
                rsp.results.get(3).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(4).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("12.50"));
                         add(new Text("14.25"));
                         add(new Text("11.00"));
                         add(new Text("12.25")); }},
                rsp.results.get(5).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(6).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("2"));
                         add(new Text("4"));
                         add(new Text("4"));
                         add(new Text("3")); }},
                rsp.results.get(7).table
        );
    }

    static final String sampleNoHeader = "" +
            "\"martini\",2,12.50,\"2020-01-01\"\n" +
            "\"sazerac\",4,14.25,\"2020-02-02\"\n" +
            "\"cosmopolitan\",4,11.00,\"2020-03-03\"\n" +
            "\"bellini\",3,12.25,\"2020-04-04\"\n";


    @Test void testLoadCsvNoHeader() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var opts = new CsvOptions().withHeaderRow(0);
        var loadRsp = client.loadCsv(
                databaseName, engineName, "sample_no_header", sampleNoHeader, opts);
        assertEquals("COMPLETED", loadRsp.transaction.state);
        assertEquals(0, loadRsp.problems.size());

        var rsp = client.execute(databaseName, engineName, "def output = sample_no_header");

        assertEquals(new ArrayList<Object>() {{ add(1L); add(2L); add(3L); add(4L);}}, rsp.results.get(0).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("martini"));
                         add(new Text("sazerac"));
                         add(new Text("cosmopolitan"));
                         add(new Text("bellini")); }},
                rsp.results.get(1).table
        );
        assertEquals(new ArrayList<Object>() {{ add(1L); add(2L); add(3L); add(4L);}}, rsp.results.get(2).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("2"));
                         add(new Text("4"));
                         add(new Text("4"));
                         add(new Text("3")); }},
                rsp.results.get(3).table
        );
        assertEquals(new ArrayList<Object>() {{ add(1L); add(2L); add(3L); add(4L);}}, rsp.results.get(4).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("12.50"));
                         add(new Text("14.25"));
                         add(new Text("11.00"));
                         add(new Text("12.25")); }},
                rsp.results.get(5).table
        );
        assertEquals(new ArrayList<Object>() {{ add(1L); add(2L); add(3L); add(4L);}}, rsp.results.get(6).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("2020-01-01"));
                         add(new Text("2020-02-02"));
                         add(new Text("2020-03-03"));
                         add(new Text("2020-04-04")); }},
                rsp.results.get(7).table
        );
    }

    static final String sampleAltSyntax = "" +
            "cocktail|quantity|price|date\n" +
            "'martini'|2|12.50|'2020-01-01'\n" +
            "'sazerac'|4|14.25|'2020-02-02'\n" +
            "'cosmopolitan'|4|11.00|'2020-03-03'\n" +
            "'bellini'|3|12.25|'2020-04-04'\n";

    @Test void testLoadCsvAltSyntax() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var opts = new CsvOptions().withDelim('|').withQuoteChar('\'');
        var loadRsp = client.loadCsv(
                databaseName, engineName, "sample_alt_syntax", sampleAltSyntax, opts);
        assertEquals("COMPLETED", loadRsp.transaction.state);
        assertEquals(0, loadRsp.problems.size());

        var rsp = client.execute(databaseName, engineName, "def output = sample_alt_syntax");

        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(0).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("martini"));
                         add(new Text("sazerac"));
                         add(new Text("cosmopolitan"));
                         add(new Text("bellini")); }},
                rsp.results.get(1).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(2).table);

        assertEquals(new ArrayList<Object>() {{
                         add(new Text("2020-01-01"));
                         add(new Text("2020-02-02"));
                         add(new Text("2020-03-03"));
                         add(new Text("2020-04-04")); }},
                rsp.results.get(3).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(4).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("12.50"));
                         add(new Text("14.25"));
                         add(new Text("11.00"));
                         add(new Text("12.25")); }},
                rsp.results.get(5).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(6).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("2"));
                         add(new Text("4"));
                         add(new Text("4"));
                         add(new Text("3")); }},
                rsp.results.get(7).table
        );
    }

    @Test void testLoadCsvWithSchema() throws HttpError, InterruptedException, IOException {
        var client = createClient();

        ensureDatabase(client);

        var schema = new HashMap<String, String>();
        schema.put("cocktail", "string");
        schema.put("quantity", "int");
        schema.put("price", "decimal(64,2)");
        schema.put("date", "date");

        var opts = new CsvOptions().withSchema(schema);
        var loadRsp = client.loadCsv(databaseName, engineName, "sample", sample, opts);
        assertEquals("COMPLETED", loadRsp.transaction.state);
        assertEquals(0, loadRsp.problems.size());

        var rsp = client.execute(databaseName, engineName, "def output = sample");

        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(0).table);
        assertEquals(new ArrayList<Object>() {{
                         add(new Text("martini"));
                         add(new Text("sazerac"));
                         add(new Text("cosmopolitan"));
                         add(new Text("bellini")); }},
                rsp.results.get(1).table
        );
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(2).table);
        assertEquals(new ArrayList<Object>() {{ add(737425L); add(737457L); add(737487L); add(737519L); }}, rsp.results.get(3).table);
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(4).table);
        assertEquals(new ArrayList<Object>() {{ add(1250L); add(1425L); add(1100L); add(1225L); }}, rsp.results.get(5).table);
        assertEquals(new ArrayList<Object>() {{ add(2L); add(3L); add(4L); add(5L);}}, rsp.results.get(6).table);
        assertEquals(new ArrayList<Object>() {{ add(2L); add(4L); add(4L); add(3L); }}, rsp.results.get(7).table);
    }

    @AfterAll
    void tearDown() throws IOException, HttpError, InterruptedException {
        var client = createClient();
        var deleteRsp = client.deleteDatabase(databaseName);
        assertEquals(databaseName, deleteRsp.name);
    }
}
