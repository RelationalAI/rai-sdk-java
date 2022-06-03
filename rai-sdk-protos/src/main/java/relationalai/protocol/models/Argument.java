package com.relationalai.protos.models;

import com.jsoniter.annotation.JsonProperty;

public class Argument{
    @JsonProperty(value = "tag")
    public String tag;
    @JsonProperty(value = "constantType")
    public ConstantType constantType;
    @JsonProperty(value = "primitiveType")
    public String primitiveType;
    @JsonProperty(value = "stringVal")
    public String stringVal;
}