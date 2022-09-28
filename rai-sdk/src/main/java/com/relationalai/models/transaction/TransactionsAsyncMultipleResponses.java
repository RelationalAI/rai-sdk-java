package com.relationalai.models.transaction;

import com.jsoniter.annotation.JsonProperty;
import com.relationalai.models.Entity;
import com.relationalai.models.transaction.TransactionAsyncResponse;

import java.util.List;

public class TransactionsAsyncMultipleResponses extends Entity {
    @JsonProperty(value = "transactions", required = true)
    public List<TransactionAsyncResponse> transactions;
}
