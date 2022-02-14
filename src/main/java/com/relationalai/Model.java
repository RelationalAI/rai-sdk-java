package com.relationalai;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Model {
    public abstract void init(JSONObject data);

    public static List<String> asStringList(JSONArray array) {
        List<String> result = new ArrayList<String>();
        for (Object obj : array)
            result.add((String) obj);
        return result;
    }

    public static List<String> asStringList(JSONObject obj, String key) {
        return asStringList(obj.getJSONArray(key));
    }

    public static List<Integer> asIntList(JSONArray array) {
        List<Integer> result = new ArrayList<Integer>();
        for (Object obj : array)
            result.add((Integer) obj);
        return result;
    }

    public static List<Integer> asIntList(JSONObject obj, String key) {
        return asIntList(obj.getJSONArray(key));
    }

    public static <T extends Model> List<T> asModelList(JSONArray array, Class<T> cls) {
        Constructor<T> ctor = null;
        try {
            ctor = cls.getDeclaredConstructor(JSONObject.class);
        } catch (Exception e) {
            assert false;
        }
        List<T> result = new ArrayList<T>();
        try {
            for (Object obj : array) {
                T item = ctor.newInstance((JSONObject) obj);
                result.add(item);
            }
        } catch (Exception e) {
            assert false;
        }
        return result;
    }

    public static <T extends Model> List<T> asModelList(JSONObject obj, String key, Class<T> cls) {
        return asModelList(obj.getJSONArray(key), cls);
    }
}
