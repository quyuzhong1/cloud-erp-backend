package com.erp.model.wms.entity;

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
 * 仓库签收单
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("wms_warehouse_sign")
public class WmsWarehouseSignEntity extends BaseEntity<WmsWarehouseSignEntity> {

    /**
     * 签收状态（0待签收，1签收中，2已完成
     */
    @TableField("sign_status")
    private String signStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 采购订单id
     */
    @TableField("purch_order_id")
    private String purchOrderId;

    /**
     * 采购订单编号
     */
    @TableField("purch_order_code")
    private String purchOrderCode;

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 供应商名称
     */
    @TableField("supplier_name")
    private String supplierName;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 计划交期
     */
    @TableField("plan_delivery_date")
    private Date planDeliveryDate;

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
     * 采购员id
     */
    @TableField("purch_user_id")
    private String purchUserId;

    /**
     * 采购员名称
     */
    @TableField("purch_user_name")
    private String purchUserName;


    public static final String SIGN_STATUS = "sign_status";

    public static final String CODE = "code";

    public static final String PURCH_ORDER_ID = "purch_order_id";

    public static final String PURCH_ORDER_CODE = "purch_order_code";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String DEST_WAREHOUSE_ID = "dest_warehouse_id";

    public static final String DEST_WAREHOUSE_NAME = "dest_warehouse_name";

    public static final String PURCH_USER_ID = "purch_user_id";

    public static final String PURCH_USER_NAME = "purch_user_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
