package com.relationalai;

import com.relationalai.models.MetadataInfo;
import com.relationalai.proto.Message;

import java.util.List;

public class TransactionAsyncResult extends Entity {

    public TransactionAsyncCompactResponse transaction;
    public List<ArrowRelation> results;
    public List<TransactionAsyncMetadataResponse> metadata;
    public MetadataInfo metadataInfo;
    public List<Object> problems;

    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            List<TransactionAsyncMetadataResponse> metadata,
            MetadataInfo metadataInfo,
            List<Object> problems) {
        this.transaction = transaction;
        this.results = results;
        this.metadata = metadata;
        this.metadataInfo = metadataInfo;
        this.problems = problems;
    }
}
