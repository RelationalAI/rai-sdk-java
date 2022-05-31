package com.relationalai.models;

import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class RelationId{
    @JsonProperty(value = "arguments")
    public List<Argument> arguments;
}