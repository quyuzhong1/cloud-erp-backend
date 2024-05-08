package com.erp.model.oms.entity;

import java.math.BigDecimal;
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
 * 申报规则表
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_declare")
public class CfgDeclareEntity extends BaseEntity<CfgDeclareEntity> {

    /**
    * 规则名称
    */
    @TableField("name")
    private String name;
    /**
    * 禁用状态 false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 描述
    */
    @TableField("remark")
    private String remark;
    /**
    * 报关中文名称
    */
    @TableField("declare_chinese_name")
    private String declareChineseName;
    /**
    * 报关英文名称
    */
    @TableField("declare_english_name")
    private String declareEnglishName;
    /**
    * 目的国海关编码
    */
    @TableField("to_customs_code")
    private String toCustomsCode;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 目的国申报价
    */
    @TableField("to_declare_price")
    private BigDecimal toDeclarePrice;
    /**
    * 目的国申报币种
    */
    @TableField("to_currency")
    private String toCurrency;
    /**
    * 目的国申报币种符号
    */
    @TableField("to_currency_symbol")
    private String toCurrencySymbol;
    /**
    * 目的国申报类型（dictType=toDeclarePriceType）
    */
    @TableField("to_declare_price_type")
    private String toDeclarePriceType;
    /**
    * 固定比例（当toDeclarePriceType=ratePrice）必填
    */
    @TableField("rate")
    private BigDecimal rate;
    /**
    * 最高申报价
    */
    @TableField("max_declare_price")
    private BigDecimal maxDeclarePrice;
    /**
    * 最低申报价
    */
    @TableField("min_declare_price")
    private BigDecimal minDeclarePrice;


    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String DECLARE_CHINESE_NAME = "declare_chinese_name";

    public static final String DECLARE_ENGLISH_NAME = "declare_english_name";

    public static final String TO_CUSTOMS_CODE = "to_customs_code";

    public static final String PRIORITY = "priority";

    public static final String TO_DECLARE_PRICE = "to_declare_price";

    public static final String TO_CURRENCY = "to_currency";

    public static final String TO_CURRENCY_SYMBOL = "to_currency_symbol";

    public static final String TO_DECLARE_PRICE_TYPE = "to_declare_price_type";

    public static final String RATE = "rate";

    public static final String MAX_DECLARE_PRICE = "max_declare_price";

    public static final String MIN_DECLARE_PRICE = "min_declare_price";

    @Override
    public Serializable pkVal() {
        return null;
    }

}