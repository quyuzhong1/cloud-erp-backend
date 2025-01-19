package com.erp.model.mrp.dto;

import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class InventoryTotalDTO {

    /**
     * 类型
     * @see ReplenishmentInventoryTypeEnum
     */
    @NotBlank(message = "库存类型不能为空")
    private String type;
    /**
     * 详细id
     */
    private String detailId;
    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 是否当前店铺
     */
    private Boolean currentShop;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 是否过滤0
     */
    private Boolean isShowZeroInventory;
}
