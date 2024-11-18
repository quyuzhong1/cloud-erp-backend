package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


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
@TableName("shipping_template_rule")
public class ShippingTemplateRuleEntity extends BaseEntity<ShippingTemplateRuleEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 起始地
    */
    @TableField("from_country")
    private String fromCountry;
    /**
    * 目的地
    */
    @TableField("to_country")
    private String toCountry;
    /**
    * 分区
    */
    @TableField("region")
    private String region;
    /**
    * 目的仓库
    */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
    /**
    * 开始重量
    */
    @TableField("start_weight")
    private BigDecimal startWeight;
    /**
    * 结束重量
    */
    @TableField("end_weight")
    private BigDecimal endWeight;
    /**
    * 首重
    */
    @TableField("first_weight")
    private BigDecimal firstWeight;
    /**
    * 首重运费
    */
    @TableField("first_weight_shipping_cost")
    private BigDecimal firstWeightShippingCost;
    /**
    * 续重单位重量
    */
    @TableField("additional_unit_weight")
    private BigDecimal additionalUnitWeight;
    /**
    * 续重单价
    */
    @TableField("additional_price")
    private BigDecimal additionalPrice;
    /**
     * 运费单价
     */
    @TableField("shipping_price")
    private BigDecimal shippingPrice;
    /**
    * 挂号费
    */
    @TableField(value = "registration_cost")
    private BigDecimal registrationCost;
    /**
    * 操作费
    */
    @TableField(value = "operating_cost")
    private BigDecimal operatingCost;
    /**
    * 最低收费
    */
    @TableField(value = "min_cost")
    private BigDecimal minCost;

    /**
     * 起始地名称
     */
    @TableField(exist = false)
    private String fromCountryName;
    /**
     * 目的地名称
     */
    @TableField(exist = false)
    private String toCountryName;
    /**
     * 城市
     */
    @TableField(exist = false)
    private List<String> cityList;

    public static final String MAIN_ID = "main_id";

    public static final String FROM_COUNTRY = "from_country";

    public static final String TO_COUNTRY = "to_country";

    public static final String FIELD_REGION = "region";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String START_WEIGHT = "start_weight";

    public static final String END_WEIGHT = "end_weight";

    public static final String FIRST_WEIGHT = "first_weight";

    public static final String FIRST_WEIGHT_SHIPPING_COST = "first_weight_shipping_cost";

    public static final String ADDITIONAL_UNIT_WEIGHT = "additional_unit_weight";

    public static final String ADDITIONAL_PRICE = "additional_price";

    public static final String REGISTRATION_COST = "registration_cost";

    public static final String OPERATING_COST = "operating_cost";

    public static final String MIN_COST = "min_cost";

    @Override
    public Serializable pkVal() {
        return null;
    }

}