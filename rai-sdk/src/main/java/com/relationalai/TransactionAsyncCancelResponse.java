package com.relationalai;

import com.jsoniter.annotation.JsonProperty;

public class TransactionAsyncCancelResponse extends Entity {
    @JsonProperty(value = "message")
    public String message;
}
