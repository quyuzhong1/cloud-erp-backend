package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 运费模板渠道关联表
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shipping_template_ref_channel")
public class ShippingTemplateRefChannelEntity extends BaseEntity<ShippingTemplateRefChannelEntity> {

    /**
    * 主表id 模板id
    */
    @TableField("main_id")
    private String mainId;

    /**
     * 模板名称
     */
    @TableField(exist = false)
    private String shippingTemplateName;

    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;


    public static final String MAIN_ID = "main_id";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}