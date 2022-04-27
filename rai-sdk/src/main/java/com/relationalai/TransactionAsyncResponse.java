package com.relationalai;

import com.jsoniter.annotation.JsonProperty;

public class TransactionAsyncResponse extends TransactionAsyncCompactResponse {
    @JsonProperty(value = "account_name", required = true)
    public String accountName;

    @JsonProperty(value = "created_by", required = true)
    public String createdBy;

    @JsonProperty(value = "created_on", required = true)
    public String createdOn;

    @JsonProperty(value = "database_name", required = true)
    public String databaseName;

    @JsonProperty(value = "read_only", required = true)
    public boolean readOnly;

    @JsonProperty(value = "query", required = true)
    public String query;

    @JsonProperty(value = "last_requested_interval", required = true)
    public String lastRequestedInterval;

    public TransactionAsyncResponse() {}
    public TransactionAsyncResponse(
            String id,
            String state,
            String accountName,
            String createdBy,
            String createdOn,
            String databaseName,
            boolean readOnly,
            String query,
            String lastRequestedInterval
    ) {
        super(id, state);
        this.accountName = accountName;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.databaseName = databaseName;
        this.readOnly = readOnly;
        this.query = query;
        this.lastRequestedInterval = lastRequestedInterval;
    }
}
