package com.relationalai;

import com.jsoniter.annotation.JsonProperty;

public class TransactionAsyncCompactResponse extends Entity {
    @JsonProperty(value = "id", required = true)
    public String id;

    @JsonProperty(value = "state", required = true)
    public String state;

    public TransactionAsyncCompactResponse() {}
    public TransactionAsyncCompactResponse(String id, String state) {
        this.id = id;
        this.state = state;
    }
}
