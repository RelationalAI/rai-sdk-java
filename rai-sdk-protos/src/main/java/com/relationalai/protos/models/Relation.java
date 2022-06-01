package com.relationalai.protos.models;

import com.jsoniter.annotation.JsonProperty;

public class Relation {
    @JsonProperty(value = "relationId")
    public RelationId relationId;
    @JsonProperty(value = "fileName")
    public String fileName;
}
