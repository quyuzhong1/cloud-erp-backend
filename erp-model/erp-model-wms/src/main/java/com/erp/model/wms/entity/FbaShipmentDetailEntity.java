package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * FBI拣货明细表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_shipment_detail")
public class FbaShipmentDetailEntity extends BaseEntity<FbaShipmentDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 平台产品id（ASIN）
    */
    @TableField("asin")
    private String asin;
    /**
    * 平台sku（mSku）
    */
    @TableField("msku")
    private String msku;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * ERP的SKU
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 收发差异
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 是否组合品
    */
    @TableField("is_combination")
    private Boolean isCombination;
    /**
     * 收货数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 最新收货日期
     */
    @TableField("receive_date")
    private LocalDateTime receiveDate;

    /**
     * 发货数量
     */
    @TableField("delivery_qty")
    private Integer deliveryQty;


    public static final String MAIN_ID = "main_id";


    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String IS_COMBO = "is_combo";


    @Override
    public String toString() {
        return "FbaShipmentDetailEntity{" +
                "msku='" + msku + '\'' +
                ", fnSku='" + fnSku + '\'' +
                ", declareQty=" + declareQty +
                ", diffQty=" + diffQty +
                ", receiveQty=" + receiveQty +
                ", receiveDate=" + receiveDate +
                '}';
    }
}