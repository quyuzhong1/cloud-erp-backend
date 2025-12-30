package com.erp.sdk.oms.amz.spapi.model.awd;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @Author: wtr
 * @Date: 2025/12/25 11:33
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
public class AwdOutStockInventorySummary {

    @SerializedName("sku")
    private String sku = null;

    @SerializedName("availableDistributableQuantity")
    private Integer availableDistributableQuantity = null;

    @SerializedName("replenishmentQuantity")
    private Integer replenishmentQuantity = null;

    @SerializedName("reservedDistributableQuantity")
    private Integer reservedDistributableQuantity = null;

    @SerializedName("totalInboundQuantity")
    private Integer totalInboundQuantity = null;

    @SerializedName("totalOnhandQuantity")
    private Integer totalOnhandQuantity = null;
}
