package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_application_detail")
public class ScmPurchaseApplicationDetailEntity extends BaseEntity<ScmPurchaseApplicationDetailEntity> {

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
     * 单箱数量
     */
    @TableField("unit_qty")
    private Integer unitQty;

    /**
     * 是否加急（false否，true是）
     */
    @TableField("is_urgent")
    private Boolean isUrgent;

    /**
     * 计划交期
     */
    @TableField("plan_delivery_date")
    private Date planDeliveryDate;

    /**
     * 申请数量
     */
    @TableField("apply_qty")
    private Integer applyQty;

    /**
     * 目的仓库id
     */
    @TableField("dest_warehouse_id")
    private String destWarehouseId;

    /**
     * 目的仓库名称
     */
    @TableField("dest_warehouse_name")
    private String destWarehouseName;

    /**
     * 采购组织id
     */
    @TableField("purchase_org_id")
    private String purchaseOrgId;

    /**
     * 采购组织名称
     */
    @TableField("purchase_org_name")
    private String purchaseOrgName;

    /**
     * 收料组织id
     */
    @TableField("receive_org_id")
    private String receiveOrgId;

    /**
     * 收料组织名称
     */
    @TableField("receive_org_name")
    private String receiveOrgName;

    /**
     * 实际采购数量
     */
    @TableField("real_purchase_qty")
    private Integer realPurchaseQty;

    /**
     * 签收数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 入库数量
     */
    @TableField("stock_in_qty")
    private Integer stockInQty;

    /**
     * 是否生成采购订单（false否，true是）
     */
    @TableField("is_create_po")
    private Boolean isCreatePo;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String UNIT_QTY = "unit_qty";

    public static final String IS_URGENT = "is_urgent";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String APPLY_QTY = "apply_qty";

    public static final String DEST_WAREHOUSE_ID = "dest_warehouse_id";

    public static final String DEST_WAREHOUSE_NAME = "dest_warehouse_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String RECEIVE_ORG_ID = "receive_org_id";

    public static final String RECEIVE_ORG_NAME = "receive_org_name";

    public static final String REAL_PURCHASE_QTY = "real_purchase_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String STOCK_IN_QTY = "stock_in_qty";

    public static final String IS_CREATE_PO = "is_create_po";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
