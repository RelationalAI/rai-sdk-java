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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// Test loading CSV data.
@TestInstance(Lifecycle.PER_CLASS)
public class LoadCsvTest extends UnitTest {
    Relation findRelation(Relation[] relations, String colName) {
        for (var relation : relations) {
            var keys = relation.relKey.keys;
            if (keys.length == 0)
                continue;
            var name = keys[0];
            if (name.equals(colName))
                return relation;
        }
        return null;
    }

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
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        var rsp = client.execute(databaseName, engineName, "def output = sample");

        Relation rel;

        rel = findRelation(rsp.output, ":date");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"2020-01-01", "2020-02-02", "2020-03-03", "2020-04-04"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":price");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"12.50", "14.25", "11.00", "12.25"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":quantity");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"2", "4", "4", "3"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":cocktail");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"martini", "sazerac", "cosmopolitan", "bellini"}
        }, rel.columns);
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
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        var rsp = client.execute(databaseName, engineName, "def output = sample_no_header");

        Relation rel;

        rel = findRelation(rsp.output, ":COL1");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {0., 31., 62., 98.},
                {"martini", "sazerac", "cosmopolitan", "bellini"}
        }, rel.columns);


        rel = findRelation(rsp.output, ":COL2");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {0., 31., 62., 98.},
                {"2", "4", "4", "3"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":COL3");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {0., 31., 62., 98.},
                {"12.50", "14.25", "11.00", "12.25"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":COL4");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {0., 31., 62., 98.},
                {"2020-01-01", "2020-02-02", "2020-03-03", "2020-04-04"}
        }, rel.columns);
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
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        var rsp = client.execute(databaseName, engineName, "def output = sample_alt_syntax");

        Relation rel;

        rel = findRelation(rsp.output, ":date");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"2020-01-01", "2020-02-02", "2020-03-03", "2020-04-04"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":price");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"12.50", "14.25", "11.00", "12.25"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":quantity");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"2", "4", "4", "3"}
        }, rel.columns);

        rel = findRelation(rsp.output, ":cocktail");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"martini", "sazerac", "cosmopolitan", "bellini"}
        }, rel.columns);
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
        assertEquals(false, loadRsp.aborted);
        assertEquals(0, loadRsp.output.length);
        assertEquals(0, loadRsp.problems.length);

        var rsp = client.execute(databaseName, engineName, "def output = sample");
        Json.print(rsp, 4);

        Relation rel;

        rel = findRelation(rsp.output, ":date");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"2020-01-01", "2020-02-02", "2020-03-03", "2020-04-04"}
        }, rel.columns);
        assertEquals(1, rel.relKey.values.length);
        assertEquals("Dates.Date", rel.relKey.values[0]);

        rel = findRelation(rsp.output, ":price");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {12.5, 14.25, 11.00, 12.25}
        }, rel.columns);
        assertEquals(1, rel.relKey.values.length);
        assertEquals("FixedPointDecimals.FixedDecimal{Int64, 2}", rel.relKey.values[0]);

        rel = findRelation(rsp.output, ":quantity");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {2., 4., 4., 3.}
        }, rel.columns);
        assertEquals(1, rel.relKey.values.length);
        assertEquals("Int64", rel.relKey.values[0]);

        rel = findRelation(rsp.output, ":cocktail");
        assertNotNull(rel);
        assertEquals(2, rel.columns.length);
        assertArrayEquals(new Object[][] {
                {29., 60., 91., 127.},
                {"martini", "sazerac", "cosmopolitan", "bellini"}
        }, rel.columns);
        assertEquals(1, rel.relKey.values.length);
        assertEquals("String", rel.relKey.values[0]);
    }
}
