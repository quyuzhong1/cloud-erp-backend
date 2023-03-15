package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_change_detail")
public class ScmPurchaseChangeDetailEntity extends BaseEntity<ScmPurchaseChangeDetailEntity> {

    /**
     * 采购变更单id
     */
    @TableField("purch_change_id")
    private String purchChangeId;

    /**
     * 采购订单明细id
     */
    @TableField("purch_order_detail_id")
    private String purchOrderDetailId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 原采购数量
     */
    @TableField("old_qty")
    private Integer oldQty;

    /**
     * 原含税单价
     */
    @TableField("old_price")
    private BigDecimal oldPrice;

    /**
     * 原含税金额
     */
    @TableField("old_amount")
    private BigDecimal oldAmount;

    /**
     * 新采购数量
     */
    @TableField("new_qty")
    private Integer newQty;

    /**
     * 新含税单价
     */
    @TableField("new_price")
    private BigDecimal newPrice;

    /**
     * 新含税金额
     */
    @TableField("new_amount")
    private BigDecimal newAmount;

    /**
     * 变更备注
     */
    @TableField("remark")
    private String remark;


    public static final String PURCH_CHANGE_ID = "purch_change_id";

    public static final String PURCH_ORDER_DETAIL_ID = "purch_order_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String OLD_QTY = "old_qty";

    public static final String OLD_PRICE = "old_price";

    public static final String OLD_AMOUNT = "old_amount";

    public static final String NEW_QTY = "new_qty";

    public static final String NEW_PRICE = "new_price";

    public static final String NEW_AMOUNT = "new_amount";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
