package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class PagingBean {
    /**
     * total : 2
     * limit : 1
     * offset : 1
     */

    @SerializedName("total")
    private int total;
    @SerializedName("limit")
    private int limit;
    @SerializedName("offset")
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
