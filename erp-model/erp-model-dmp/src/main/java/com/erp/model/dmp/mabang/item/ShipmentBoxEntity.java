package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @CreateTime: 2023-06-29  16:26
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@ToString
public class ShipmentBoxEntity implements Serializable {

    /**
     * 箱子编号
     */
    private String packNo;

    /**
     * SKU信息
     */
    private List<ShipmentSkuEntity> skuInfo;

    /**
     * 箱子毛重
     */
    private String weight;

    /**
     * 箱子长
     */
    private String length;

    /**
     * 箱子宽
     */
    private String width;

    /**
     * 箱子高
     */
    private String height;

    /**
     * 货运单号
     */
    private String trackingId;

    /**
     * 发货仓库
     */
    private String fbaWarehouseId;

    /**
     * 发货仓库
     */
    private String warehouseId;

    /**
     * 发货仓库编码
     */
    private String warehouseCode;


}