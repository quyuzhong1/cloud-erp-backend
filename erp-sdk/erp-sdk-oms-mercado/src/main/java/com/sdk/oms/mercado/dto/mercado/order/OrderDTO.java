package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {


    @SerializedName("query")
    private String query;
    @SerializedName("sort")
    private SortBean sort;
    @SerializedName("paging")
    private PagingBean paging;
    @SerializedName("results")
    private List<ResultsBean> results;
    @SerializedName("available_sorts")
    private List<AvailableSortsBean> availableSorts;
    @SerializedName("filters")
    private List<FiltersBean> filters;

}
