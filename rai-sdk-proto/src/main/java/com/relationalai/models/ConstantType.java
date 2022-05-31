package com.relationalai.models;

import com.jsoniter.annotation.JsonProperty;

public class ConstantType{
    @JsonProperty(value = "relType")
    public RelType relType;
    @JsonProperty(value = "value")
    public Value value;
}