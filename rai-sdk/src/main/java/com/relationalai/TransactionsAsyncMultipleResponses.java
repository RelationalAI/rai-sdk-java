package com.relationalai;

import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class TransactionsAsyncMultipleResponses extends Entity {
    @JsonProperty(value = "transactions", required = true)
    public List<TransactionAsyncResponse> transactions;
}
