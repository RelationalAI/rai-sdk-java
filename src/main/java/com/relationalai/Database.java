package com.relationalai;

import org.json.JSONObject;

public class Database extends Model {
    public String Id;
    public String Name;
    public String Region;
    public String AccountName;
    public String CreatedBy;
    public String DeletedOn;
    public String DeletedBy;
    public String DefaultEngine;
    public String State;

    public Database() {
    }

    public Database(JSONObject obj) {
        init(obj);
    }

    public void init(JSONObject obj) {
        Id = obj.getString("id");
        Name = obj.getString("name");
        Region = obj.getString("region");
        AccountName = obj.getString("account_name");
        CreatedBy = obj.getString("created_by");
        DeletedOn = obj.getString("deleted_on");
        DeletedBy = obj.optString("deleted_by", null);
        DefaultEngine = obj.optString("default_compute_name", null);
        State = obj.getString("state");
    }
}
