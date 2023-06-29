package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @CreateTime: 2023-06-29  16:09
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@ToString
public class ShipmentItemEntity {

    /**
     * 平台SKU
     */
    private String platformSku;

    /**
     * fbaStock.id
     */
    private String fbaStockId;

    /**
     * 申报返回数量
     */
    private String applyQuantity;

    /**
     * 发货数量
     */
    private String deliveryQuantity;

    /**
     * 签收数量
     */
    private String receivedQuantity;

    /**
     * 产品asin
     */
    private String asin;

    /**
     * 本地库存类型
     */
    private String stockType;

    /**
     * 本地库存sku
     */
    private String stockSku;

    /**
     * FNSKU
     */
    private String FNSKU;

    /**
     * SKU图片
     */
    private String pictureUrl;

    /**
     * 仓库ID
     */
    private String warehouse_id;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 本地信息
     */
    private List<ShipmentItemLocalInfoEntity> localInfo;

    /**
     * 费用信息
     */
    private List<ShipmentCostDetailEntity> headingcostdetail;

}