package com.relationalai;

import com.relationalai.protos.models.MetadataInfoResult;
import relationalai.protocol.Message;

import java.util.List;

public class TransactionAsyncResult extends Entity {

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
    }

    @Override
    public String toString() {
        return String.format("%s\n %s\n %s\n %s", transaction, metadata, results, problems);
    }
}
