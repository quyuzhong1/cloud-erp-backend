package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 售后申请明细表
 * </p>
 *
 * @author jack
 * @since 2025-04-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("after_sale_detail")
public class AfterSaleDetailEntity extends BaseEntity<AfterSaleDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 货值
    */
    @TableField("price")
    private BigDecimal price;
    /**
    * sku维修金额
    */
    @TableField("repair_amount")
    private BigDecimal repairAmount;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * skuNo
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * skuName
    */
    @TableField("prodcut_name")
    private String prodcutName;
    /**
    * 数量
    */
    @TableField("sku_qty")
    private Integer skuQty;
    /**
    * 描述
    */
    @TableField("detail_desc")
    private String detailDesc;


    public static final String MAIN_ID = "main_id";

    public static final String PRICE = "price";

    public static final String REPAIR_AMOUNT = "repair_amount";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODCUT_NAME = "prodcut_name";

    public static final String SKU_QTY = "sku_qty";

    public static final String DETAIL_DESC = "detail_desc";

    @Override
    public Serializable pkVal() {
        return null;
    }

}