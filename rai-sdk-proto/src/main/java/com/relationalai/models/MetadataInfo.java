package com.relationalai.models;

import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class MetadataInfo {
    @JsonProperty(value = "relations")
    public List<Relation> relations;
}
