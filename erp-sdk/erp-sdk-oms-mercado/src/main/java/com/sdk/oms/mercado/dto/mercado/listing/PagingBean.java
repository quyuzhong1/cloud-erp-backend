package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class PagingBean {
    /**
     * total : 1
     * offset : 0
     * limit : 10
     */

    @SerializedName("total")
    private int total;
    @SerializedName("offset")
    private int offset;
    @SerializedName("limit")
    private int limit;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
