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
 * 发货计划详情表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_delivery_plan_detail")
public class OverseasDeliveryPlanDetailEntity extends BaseEntity<OverseasDeliveryPlanDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 计划数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 是否组合品
    */
    @TableField("is_combination")
    private Boolean isCombination;

    /**
     * 平台sku
     */
    @TableField("platform_sku")
    private String platformSku;

    /**
     * 平台sku
     */
    @TableField("platform_sku_name")
    private String platformSkuName;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String IS_COMBINATION = "is_combination";

    @Override
    public Serializable pkVal() {
        return null;
    }

}