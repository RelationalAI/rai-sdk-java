package com.relationalai.models.transaction;

import com.jsoniter.annotation.JsonProperty;
import com.relationalai.models.Entity;
import com.relationalai.models.transaction.TransactionAsyncResponse;

public class TransactionAsyncSingleResponse extends Entity {
    @JsonProperty(value = "transaction", required = true)
    public TransactionAsyncResponse transaction;
}
