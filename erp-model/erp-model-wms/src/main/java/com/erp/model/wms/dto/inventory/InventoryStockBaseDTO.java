package com.erp.model.wms.dto.inventory;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @Classname: InventoryStockBaseDTO

 * @CreateTime: 2023-05-04  15:41
 * @Author: zhangchunlin
 */

@Data
public class InventoryStockBaseDTO implements Serializable {

    /**
     * sku id
     */
    @NotEmpty(message = "sku id不能为空")
    private String skuId;

    /**
     * sku编码
     */
    @NotEmpty(message = "sku编码不能为空")
    private String skuNo;
}