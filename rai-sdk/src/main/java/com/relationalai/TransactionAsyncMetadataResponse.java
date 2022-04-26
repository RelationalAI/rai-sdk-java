package com.relationalai;

import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class TransactionAsyncMetadataResponse extends Entity {
    @JsonProperty(value = "relationId", required = true)
    public String relationId;

    @JsonProperty(value = "types", required = true)
    public List<String> types;

    public TransactionAsyncMetadataResponse() {}
    public TransactionAsyncMetadataResponse(String relationId, List<String> types) {
        this.relationId = relationId;
        this.types = types;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof TransactionAsyncMetadataResponse)) {
            return false;
        }
        var that = (TransactionAsyncMetadataResponse) o;

        return this.relationId.equals(that.relationId) && this.types.equals(that.types);
    }
}
