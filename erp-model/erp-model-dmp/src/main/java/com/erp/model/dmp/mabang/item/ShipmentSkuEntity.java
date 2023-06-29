package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @CreateTime: 2023-06-29  16:24
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@ToString
public class ShipmentSkuEntity implements Serializable {

    /**
     * 发货数量
     */
    private String delieverQuantity;

    /**
     * 平台SKU
     */
    private String platformSku;

}