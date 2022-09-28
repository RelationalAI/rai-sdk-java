package com.relationalai.models.transaction;

import com.jsoniter.annotation.JsonProperty;
import com.relationalai.models.Entity;

public class TransactionAsyncCancelResponse extends Entity {
    @JsonProperty(value = "message")
    public String message;
}
