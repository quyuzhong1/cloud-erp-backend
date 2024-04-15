package com.erp.model.oms.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class SplittableGroupsBean {
    /**
     * id : 12333
     * order_line_item_ids : ["578728817428498442","578728817428695050"]
     */

    @SerializedName("id")
    private String id;
    @SerializedName("order_line_item_ids")
    private List<String> orderLineItemIds;
}
