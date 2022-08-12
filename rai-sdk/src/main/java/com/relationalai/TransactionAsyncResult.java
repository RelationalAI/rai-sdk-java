package com.relationalai;

import java.util.List;

public class TransactionAsyncResult extends Entity {

    public Boolean gotCompleteResult;
    public TransactionAsyncCompactResponse transaction;
    public List<ArrowRelation> results;
    public List<TransactionAsyncMetadataResponse> metadata;
    public List<Object> problems;

    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            List<TransactionAsyncMetadataResponse> metadata,
            List<Object> problems
    ) {
        this.transaction = transaction;
        this.results = results;
        this.metadata = metadata;
        this.problems = problems;
        this.gotCompleteResult = false;
    }
    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            List<TransactionAsyncMetadataResponse> metadata,
            List<Object> problems,
            Boolean gotCompleteResult
    ) {
        this(transaction, results, metadata, problems);
        this.gotCompleteResult = gotCompleteResult;
    }
}
