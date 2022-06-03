package com.relationalai.protos.models;

import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class MetadataInfoResult {
    @JsonProperty(value = "relations")
    public List<Relation> relations;
}
