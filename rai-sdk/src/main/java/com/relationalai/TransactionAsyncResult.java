package com.relationalai;

import com.relationalai.protos.models.MetadataInfoResult;

import java.util.List;

public class TransactionAsyncResult extends Entity {

    public TransactionAsyncCompactResponse transaction;
    public List<ArrowRelation> results;
    public List<TransactionAsyncMetadataResponse> metadata;
    public MetadataInfoResult metadataInfoResult;
    public List<Object> problems;

    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            List<TransactionAsyncMetadataResponse> metadata,
            MetadataInfoResult metadataInfoResult,
            List<Object> problems) {
        this.transaction = transaction;
        this.results = results;
        this.metadata = metadata;
        this.metadataInfoResult = metadataInfoResult;
        this.problems = problems;
    }
}
