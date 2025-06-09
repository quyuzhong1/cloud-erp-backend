package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 运费模板
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shipping_template")
public class ShippingTemplateEntity extends BaseEntity<ShippingTemplateEntity> {

    /**
    * 模板名称
    */
    @TableField("name")
    private String name;
    /**
    * 计费方式
    */
    @TableField("billing_method")
    private String billingMethod;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 价格进制
    */
    @TableField("price_binary")
    private String priceBinary;
    /**
    * 材积设置
    */
    @TableField("volume_setting")
    private Integer volumeSetting;
    /**
    * 生效日期
    */
    @TableField("effective_date")
    private LocalDate effectiveDate;
    /**
    * 失效日期
    */
    @TableField(value = "expire_date", fill = FieldFill.INSERT_UPDATE)
    private LocalDate expireDate;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 模板类型
    */
    @TableField("type")
    private String type;
    /**
     * 渠道ID
     */
    @TableField(exist = false)
    private String logisticsChannelId;


    public static final String FIELD_NAME = "name";

    public static final String BILLING_METHOD = "billing_method";

    public static final String FIELD_CURRENCY = "currency";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String PRICE_BINARY = "price_binary";

    public static final String VOLUME_SETTING = "volume_setting";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String FIELD_DISABLED = "disabled";

    public static final String FIELD_TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}