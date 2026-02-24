package com.erp.sdk.oms.amz.spapi.model.awdinventory;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @Author: wtr
 * @Date: 2025/12/25 9:58
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
public class InventoryDetails {

    /**
     * AWD可用
     */
    @SerializedName("availableDistributableQuantity")
    private Integer availableDistributableQuantity = null;

    /**
     * AWD发FBA在途
     */
    @SerializedName("replenishmentQuantity")
    private Integer replenishmentQuantity = null;

    /**
     * AWD待发货
     */
    @SerializedName("reservedDistributableQuantity")
    private Integer reservedDistributableQuantity = null;

}
