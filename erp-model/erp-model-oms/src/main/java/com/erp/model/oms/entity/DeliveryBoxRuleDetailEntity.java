package com.erp.model.oms.entity;

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
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("delivery_box_rule_detail")
public class DeliveryBoxRuleDetailEntity extends BaseEntity<DeliveryBoxRuleDetailEntity> {

    /**
     * 箱规id
     */
    @TableField("main_id")
    private String mainId;

    /**
    * 发货skuId
    */
    @TableField("delivery_sku_id")
    private String deliverySkuId;
    /**
    * 发货sku编码
    */
    @TableField("delivery_sku_no")
    private String deliverySkuNo;
    /**
    * 发货sku名称
    */
    @TableField("delivery_product_name")
    private String productName;
    /**
    * 每箱数量
    */
    @TableField("per_box_qty")
    private Integer perBoxQty;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PER_BOX_QTY = "per_box_qty";

    public static final String PRIORITY = "priority";

    @Override
    public Serializable pkVal() {
        return null;
    }

}