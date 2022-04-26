package com.relationalai;

import com.jsoniter.annotation.JsonProperty;

public class TransactionAsyncSingleResponse extends Entity {
    @JsonProperty(value = "transaction", required = true)
    public TransactionAsyncResponse transaction;
}
