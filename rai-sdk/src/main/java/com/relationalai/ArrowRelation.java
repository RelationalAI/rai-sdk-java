package com.relationalai;

import java.util.List;

public class ArrowRelation extends Entity {
    public String relationId;
    public List<Object> table;

    public ArrowRelation(String relationId, List<Object> table) {
        this.relationId = relationId;
        this.table = table;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ArrowRelation)) {
            return false;
        }
        var that = (ArrowRelation) o;

        return this.relationId.equals(that.relationId) && this.table.equals(that.table);
    }
}
