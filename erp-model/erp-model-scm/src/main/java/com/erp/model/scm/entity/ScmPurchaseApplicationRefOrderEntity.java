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
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_application_ref_order")
public class ScmPurchaseApplicationRefOrderEntity extends BaseEntity<ScmPurchaseApplicationRefOrderEntity> {

    /**
     * 采购申请明细id
     */
    @TableField("purch_application_detail_id")
    private String purchApplicationDetailId;

    /**
     * 采购申请id
     */
    @TableField("purch_application_id")
    private String purchApplicationId;

    /**
     * 采购订单明细id
     */
    @TableField("purch_order_detail_id")
    private String purchOrderDetailId;

    /**
     * 采购订单id
     */
    @TableField("purch_order_id")
    private String purchOrderId;


    public static final String PURCH_APPLICATION_DETAIL_ID = "purch_application_detail_id";

    public static final String PURCH_APPLICATION_ID = "purch_application_id";

    public static final String PURCH_ORDER_DETAIL_ID = "purch_order_detail_id";

    public static final String PURCH_ORDER_ID = "purch_order_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
