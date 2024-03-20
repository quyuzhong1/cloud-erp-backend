package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PagingBean {
    /**
     * limit : 100
     * offset : 0
     * total : 35
     */

    @JsonProperty("limit")
    private int limit;
    @JsonProperty("offset")
    private int offset;
    @JsonProperty("total")
    private int total;

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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
