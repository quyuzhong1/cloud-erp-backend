package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 物流渠道规则约束
 * </p>
 *
 * @author lrp
 * @since 2024-02-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_channel_constraint")
public class LogisticsChannelConstraintEntity extends BaseEntity<LogisticsChannelConstraintEntity> {

    /**
    * 渠道id
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 国家二字码
    */
    @TableField("country")
    private String country;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 最高报关金额
    */
    @TableField("max_customs_amount")
    private BigDecimal maxCustomsAmount;
    /**
    * 最高报关币别
    */
    @TableField("max_customs_currency")
    private String maxCustomsCurrency;
    /**
    * 最低报关金额
    */
    @TableField("min_customs_amount")
    private BigDecimal minCustomsAmount;
    /**
    * 最低报关币种
    */
    @TableField("min_customs_currency")
    private String minCustomsCurrency;
    /**
    * 重量上限
    */
    @TableField("max_weight")
    private BigDecimal maxWeight;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 长度上限
    */
    @TableField("max_length")
    private BigDecimal maxLength;
    /**
    * 宽度上限
    */
    @TableField("max_width")
    private BigDecimal maxWidth;
    /**
    * 高度上限
    */
    @TableField("max_height")
    private BigDecimal maxHeight;
    /**
    * 尺寸单位
    */
    @TableField("size_unit")
    private String sizeUnit;


    public static final String CHANNEL_ID = "channel_id";

    public static final String FIELD_COUNTRY = "country";

    public static final String COUNTRY_NAME = "country_name";

    public static final String MAX_CUSTOMS_AMOUNT = "max_customs_amount";

    public static final String MAX_CUSTOMS_CURRENCY = "max_customs_currency";

    public static final String MIN_CUSTOMS_AMOUNT = "min_customs_amount";

    public static final String MIN_CUSTOMS_CURRENCY = "min_customs_currency";

    public static final String MAX_WEIGHT = "max_weight";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String MAX_LENGTH = "max_length";

    public static final String MAX_WIDTH = "max_width";

    public static final String MAX_HEIGHT = "max_height";

    public static final String SIZE_UNIT = "size_unit";

    @Override
    public Serializable pkVal() {
        return null;
    }

}