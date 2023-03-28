package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 采购申请单和采购订单关联表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_application_ref_po")
public class PurchaseApplicationRefPoEntity extends BaseEntity<PurchaseApplicationRefPoEntity> {

    /**
     * 采购申请明细id
     */
    @TableField("purchase_application_detail_id")
    private String purchaseApplicationDetailId;

    /**
     * 采购申请id
     */
    @TableField("purchase_application_id")
    private String purchaseApplicationId;

    /**
     * 采购订单明细id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;


    public static final String PURCHASE_APPLICATION_DETAIL_ID = "purchase_application_detail_id";

    public static final String PURCHASE_APPLICATION_ID = "purchase_application_id";

    public static final String PURCHASE_ORDER_DETAIL_ID = "purchase_order_detail_id";

    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
