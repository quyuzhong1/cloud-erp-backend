package com.erp.model.tms.entity;

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
 * 运费模板其他费用选值表
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shipping_template_cost_setting")
public class ShippingTemplateCostSettingEntity extends BaseEntity<ShippingTemplateCostSettingEntity> {

    /**
    * 其他费用id
    */
    @TableField("other_cost_id")
    private String otherCostId;
    /**
    * 编码
    */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;


    public static final String OTHER_COST_ID = "other_cost_id";

    public static final String CODE = "code";

    public static final String NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}