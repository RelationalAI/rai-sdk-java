package com.relationalai.models.transaction;

import com.jsoniter.annotation.JsonProperty;

public class TransactionAsyncDeleteResponse {
    @JsonProperty(value = "txn_id", required = true)
    public String id;

    @JsonProperty(value = "message", required = true)
    public String message;

    public TransactionAsyncDeleteResponse() {}
    public TransactionAsyncDeleteResponse(String id, String state) {
        this.id = id;
        this.message = state;
    }
}