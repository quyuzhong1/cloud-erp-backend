package com.erp.model.dmp.mabang;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class ReturnOrderItemEntity {
    /**
     * 商品图片地址
     */
    private String pictureUrl;
    /**
     * 产品单位
     */
    private String productUnit;
    /**
     * 买家购买数量
     */
    private Integer quantity;
    /**
     * 退货后实际入库数量
     */
    private Integer quantity1;
    /**
     * 售价
     */
    private BigDecimal sellPrice;
    /**
     * 多物品属性
     */
    private String specifics;
    /**
     * 状态：8已收货
     */
    private Integer status;
    /**
     * 仓位
     */
    private String stockGrid;
    /**
     * 库存SKU
     */
    private String stockSku;
    /**
     * 仓库编号
     */
    private Integer stockWarehouseId;
    /**
     * 仓库名称
     */
    private String stockWarehouseName;
    /**
     * 商品名称
     */
    private String title;
    /**
     * 可退数量
     */
    private Integer quantity2;

}
