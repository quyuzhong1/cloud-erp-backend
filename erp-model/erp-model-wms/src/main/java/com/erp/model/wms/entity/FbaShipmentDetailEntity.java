package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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
    * 平台sku
    */
    @TableField("asin")
    private String asin;
    /**
    * 卖家sku
    */
    @TableField("m_sku")
    private String mSku;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
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


    public static final String MAIN_ID = "main_id";

    public static final String ASIN = "asin";

    public static final String M_SKU = "m_sku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String IS_COMBO = "is_combo";

    @Override
    public Serializable pkVal() {
        return null;
    }

}