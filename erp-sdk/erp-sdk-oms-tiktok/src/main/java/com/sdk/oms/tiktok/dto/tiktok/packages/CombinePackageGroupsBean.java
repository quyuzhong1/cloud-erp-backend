package com.sdk.oms.tiktok.dto.tiktok.packages;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class CombinePackageGroupsBean {
    /**
     * id : 12333
     * order_line_item_ids : ["578728817428498442","578728817428695050"]
     */

    @SerializedName("id")
    private String id;
    @SerializedName("order_ids")
    private List<String> orderIds;
}
