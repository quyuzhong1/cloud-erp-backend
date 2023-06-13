package com.erp.model.wms.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @CreateTime: 2023-05-17  19:14
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventorySaveDTO implements Serializable {

    /**
     * 实时库存id
     */
    private String inventoryId;

    /**
     * 原库存数量
     */
    private Integer qty;

}