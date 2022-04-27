package com.relationalai;

import java.util.List;

public class TransactionAsyncResult extends Entity {

    public TransactionAsyncCompactResponse transaction;
    public List<ArrowRelation> results;
    public List<TransactionAsyncMetadataResponse> metadata;
    public List<Object> problems;

    public TransactionAsyncResult(TransactionAsyncCompactResponse transaction, List<ArrowRelation> results, List<TransactionAsyncMetadataResponse> metadata, List<Object> problems) {
        this.transaction = transaction;
        this.results = results;
        this.metadata = metadata;
        this.problems = problems;
    }
}
