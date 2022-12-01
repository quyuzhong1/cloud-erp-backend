package com.erp.server.dmp.entity.mabang;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class OrderItemEntity {
    private BigDecimal costPrice;
    private Integer hasGoods;
    private String orderItemId;
    private Integer isCombo;
    private Integer isGift;
    private String isRepeatItem;
    private String ispaste;
    private String itemId;
    private String itemRemark;
    private String noLiquidCosmetic;
    private String originOrderId;
    private String originalPictureUrl;
    private String pictureUrl;
    private String platformFee;
    private String platformFeeOrigin;
    private Integer platformQuantity;
    private String platformSku;
    private String productUnit;
    private Integer quantity;
    private BigDecimal sellPrice;
    private BigDecimal sellPriceOrigin;
    private String specifics;
    private Integer status;
    private String stockGrid;
    private String stockSku;
    private Integer stockStatus;
    private String stockWarehouseId;
    private String storageSku;
    private String title;
    private String transactionId;
    private String unitWeight;
    private String stockWarehouseName;
    private String erpOrderItemId;
}
