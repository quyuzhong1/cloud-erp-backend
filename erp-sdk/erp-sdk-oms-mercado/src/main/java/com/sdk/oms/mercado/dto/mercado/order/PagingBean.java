package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PagingBean {
    /**
     * total : 2
     * limit : 1
     * offset : 1
     */

    @JsonProperty("total")
    private int total;
    @JsonProperty("limit")
    private int limit;
    @JsonProperty("offset")
    private int offset;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
