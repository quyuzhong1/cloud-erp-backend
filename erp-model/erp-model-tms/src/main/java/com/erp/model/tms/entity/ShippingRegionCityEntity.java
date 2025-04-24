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
 * 运费规则分区城市表
 * </p>
 *
 * @author Will
 * @since 2023-11-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shipping_region_city")
public class ShippingRegionCityEntity extends BaseEntity<ShippingRegionCityEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 规则id
    */
    @TableField("shipping_template_rule_id")
    private String shippingTemplateRuleId;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 分区
    */
    @TableField("region")
    private String region;


    public static final String MAIN_ID = "main_id";

    public static final String SHIPPING_TEMPLATE_RULE_ID = "shipping_template_rule_id";

    public static final String FIELD_CITY = "city";

    public static final String FIELD_REGION = "region";

    @Override
    public Serializable pkVal() {
        return null;
    }

}