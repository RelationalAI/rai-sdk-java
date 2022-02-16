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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Represents a "database action", which is an argument to a transaction.
class DbAction extends HashMap<String, Object> {
    DbAction() {}

    DbAction(String type) {
        put("type", type);
    }

    // Wrapps each of the given action in a LabeledAction.
    static List<DbAction> makeActions(DbAction... actions) {
        int ix = 0;
        var result = new ArrayList<DbAction>();
        for (var action : actions) {
            var item = new DbAction("LabeledAction");
            item.put("name", String.format("action%d", ix++));
            item.put("action", action);
            result.add(item);
        }
        return result;
    }

    static DbAction makeDeleteModelsAction(String[] models) {
        var result = new DbAction("ModifyWorkspaceAction");
        result.put("delete_source", models);
        return result;
    }

    static DbAction makeInstallModelAction(String name, String model) {
        var result = new DbAction("InstallAction");
        result.put("sources", makeQuerySource(name, model));
        return result;
    }

    static DbAction makeListModelsAction() {
        return new DbAction("ListSourceAction");
    }

    static DbAction makeListEdbAction() {
        return new DbAction("ListEdbAction");
    }

    static DbAction makeRelKey(String name, String key) {
        String[] keys = {key};
        String[] values = {};
        var result = new DbAction("RelKey");
        result.put("name", name);
        result.put("keys", keys);
        result.put("value", values);
        return result;
    }

    static DbAction makeQueryAction(String source, Map<String, String> inputs) {
        var actionInputs = new ArrayList<DbAction>();
        if (inputs != null) {
            for (var entry : inputs.entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                var actionInput = makeQueryActionInput(k, v);
                actionInputs.add(actionInput);
            }
        }
        String[] empty = {};
        var result = new DbAction("QueryAction");
        result.put("source", makeQuerySource("query", source));
        result.put("inputs", actionInputs);
        result.put("outputs", empty);
        result.put("persist", empty);
        return result;
    }

    static DbAction makeQueryActionInput(String name, String value) {
        String[][] columns = {{value}};
        var typename = reltype(value);
        var result = new DbAction("Relation");
        result.put("columns", columns);
        result.put("rel_key", makeRelKey(name, typename));
        return result;
    }

    static DbAction makeQuerySource(String name, String model) {
        var result = new DbAction("Source");
        result.put("name", name);
        result.put("path", "");
        result.put("value", model);
        return result;
    }

    static String reltype(Object value) {
        if (value instanceof String)
            return "RAI_VariableSizeStrings.VariableSizeString";
        throw new IllegalArgumentException("bad query input type");
    }
}
