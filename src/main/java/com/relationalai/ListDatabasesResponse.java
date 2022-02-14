package com.relationalai;

import java.util.List;

import org.json.JSONObject;

public class ListDatabasesResponse extends Model {
    public List<Database> Databases;

    public ListDatabasesResponse() {
    }

    public ListDatabasesResponse(JSONObject obj) {
        init(obj);
    }

    public void init(JSONObject obj) {
        Databases = asModelList(obj, "databases", Database.class);
    }

    public static ListDatabasesResponse fromJson(String s) {
        return new ListDatabasesResponse(new JSONObject(s));
    }
}
