package com.relationalai;

import relationalai.protocol.Message;

import java.util.List;

public class TransactionAsyncResult extends Entity {

    public Boolean gotCompleteResult;
    public TransactionAsyncCompactResponse transaction;
    public List<ArrowRelation> results;
    public Message.MetadataInfo metadata;
    public List<Object> problems;

    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            Message.MetadataInfo metadata,
            List<Object> problems) {
        this.transaction = transaction;
        this.results = results;
        this.metadata = metadata;
        this.problems = problems;
        this.gotCompleteResult = false;
    }
    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            Message.MetadataInfo metadata,
            List<Object> problems,
            Boolean gotCompleteResult
    ) {
        this(transaction, results, metadata, problems);
        this.gotCompleteResult = gotCompleteResult;
    }

    @Override
    public String toString() {
        return String.format("%s\n %s\n %s\n %s", transaction, metadata, results, problems);
    }
}
