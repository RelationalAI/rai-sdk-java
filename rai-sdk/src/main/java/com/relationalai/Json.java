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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;

public class Json {
    public static Any deserialize(String s) {
        return JsonIterator.deserialize(s);
    }

    public static <T> T deserialize(String s, Class<T> cls) {
        return JsonIterator.deserialize(s, cls);
    }

    public static void print(Object obj) {
        System.out.println(Json.toString(obj, 0));
    }

    public static void print(Object obj, int indent) {
        System.out.println(Json.toString(obj, indent));
    }

    public static String toString(Object obj) {
        return Json.serialize(obj, 0);
    }

    public static String toString(Object obj, int indent) {
        return Json.serialize(obj, indent);
    }

    public static String serialize(Object obj) { return Json.serialize(obj, 0);}

    public static String serialize(Object obj, int indent) {
        var output = new ByteArrayOutputStream();
        Json.serialize(obj, output, indent);
        return output.toString();
    }

    public static void serialize(Object obj, OutputStream output) {
        Json.serialize(obj, output, 0);
    }

    public static void serialize(Object obj, OutputStream output, int indent) {
        JsonStream.setIndentionStep(indent);
        JsonStream.serialize(obj, output);
    }
}
