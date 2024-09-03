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
}
