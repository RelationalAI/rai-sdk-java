package com.relationalai.models;

import com.jsoniter.annotation.JsonProperty;

public class RelType{
    @JsonProperty(value = "tag")
    public String tag;
    @JsonProperty(value = "primitiveType")
    public String primitiveType;
}