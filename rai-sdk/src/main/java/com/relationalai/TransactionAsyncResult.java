package com.relationalai;

import com.relationalai.proto.Message;

import java.util.List;

public class TransactionAsyncResult extends Entity {

    public TransactionAsyncCompactResponse transaction;
    public List<ArrowRelation> results;
    public List<TransactionAsyncMetadataResponse> metadata;
    public List<Message.MetadataInfo> metadataInfos;
    public List<Object> problems;

    public TransactionAsyncResult(
            TransactionAsyncCompactResponse transaction,
            List<ArrowRelation> results,
            List<TransactionAsyncMetadataResponse> metadata,
            List<Message.MetadataInfo> metadataInfos,
            List<Object> problems) {
        this.transaction = transaction;
        this.results = results;
        this.metadata = metadata;
        this.metadataInfos = metadataInfos;
        this.problems = problems;
    }
}
