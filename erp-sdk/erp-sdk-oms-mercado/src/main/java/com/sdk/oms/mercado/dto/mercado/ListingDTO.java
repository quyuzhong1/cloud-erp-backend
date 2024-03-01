package com.sdk.oms.mercado.dto.mercado;

import com.google.gson.annotations.SerializedName;
import com.sdk.oms.mercado.dto.mercado.listing.AvailableOrdersBean;
import com.sdk.oms.mercado.dto.mercado.listing.OrdersBean;
import com.sdk.oms.mercado.dto.mercado.listing.PagingBean;
import lombok.Data;

import java.util.List;

@Data
public class ListingDTO {


    /**
     * seller_id : 1509269799
     * results : ["CBT1870347315","CBT1908735880","CBT1908723878","CBT1885832065","CBT1908898756","CBT1908873808","CBT1908858870","CBT1908815982","CBT1908713864","CBT1886138265","CBT1886113867","CBT1886022863","CBT1885988119","CBT1910174848","CBT1910137756","CBT1910109186","CBT1910107728","CBT1910078390","CBT1910013762","CBT1909985010","CBT1909981644","CBT1909958736","CBT1909935734","CBT1909907752","CBT1909841788","CBT1909826082","CBT1887323327","CBT1887319969","CBT1887315245","CBT1887306439","CBT1887244953","CBT1887191741","CBT1887136377","CBT1887122441","CBT1887107185"]
     * paging : {"limit":50,"offset":0,"total":35}
     * query : null
     * orders : [{"id":"stop_time_asc","name":"Order by stop time ascending"}]
     * available_orders : [{"id":"stop_time_asc","name":"Order by stop time ascending"},{"id":"stop_time_desc","name":"Order by stop time descending"},{"id":"start_time_asc","name":"Order by start time ascending"},{"id":"start_time_desc","name":"Order by start time descending"},{"id":"available_quantity_asc","name":"Order by available quantity ascending"},{"id":"available_quantity_desc","name":"Order by available quantity descending"},{"id":"sold_quantity_asc","name":"Order by sold quantity ascending"},{"id":"sold_quantity_desc","name":"Order by sold quantity descending"},{"id":"price_asc","name":"Order by price ascending"},{"id":"price_desc","name":"Order by price descending"},{"id":"last_updated_desc","name":"Order by lastUpdated descending"},{"id":"last_updated_asc","name":"Order by last updated ascending"},{"id":"total_sold_quantity_asc","name":"Order by total sold quantity ascending"},{"id":{"id":"total_sold_quantity_desc","field":"sold_quantity","missing":"_last","order":"desc"},"name":"Order by total sold quantity descending"},{"id":{"id":"inventory_id_asc","field":"inventory_id","missing":"_last","order":"asc"},"name":"Order by inventory id ascending"}]
     */

    @SerializedName("seller_id")
    private String sellerId;
    @SerializedName("paging")
    private PagingBean paging;
    @SerializedName("query")
    private Object query;
    @SerializedName("results")
    private List<String> results;
    @SerializedName("orders")
    private List<OrdersBean> orders;
    @SerializedName("available_orders")
    private List<AvailableOrdersBean> availableOrders;

}
