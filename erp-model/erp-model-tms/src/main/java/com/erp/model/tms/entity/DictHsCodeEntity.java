package com.erp.model.tms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 出口申报要素表
 * </p>
 *
 * @author jack
 * @since 2025-07-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_hs_code")
public class DictHsCodeEntity extends BaseEntity<DictHsCodeEntity> {

    /**
    * 中国海关编码
    */
    @TableField("hs_code")
    private String hsCode;
    /**
    * 报关名
    */
    @TableField("description")
    private String description;
    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 第一法定单位
    */
    @TableField("first_declare_unit")
    private String firstDeclareUnit;
    /**
    * 第二法定单位
    */
    @TableField("second_declare_unit")
    private String secondDeclareUnit;
    /**
    * 申报要素
    */
    @TableField("declare_element")
    private String declareElement;
    /**
    * 监管条件
    */
    @TableField("supervision_conditions")
    private String supervisionConditions;
    /**
    * 最惠国税率 (%)
    */
    @TableField("mfn_rate")
    private BigDecimal mfnRate;
    /**
    * 普通税率 (%)
    */
    @TableField("general_rate")
    private BigDecimal generalRate;
    /**
    * 增值税率 (%)
    */
    @TableField("vat_rate")
    private BigDecimal vatRate;
    /**
    * 消费税率 (%)
    */
    @TableField("consumption_tax_rate")
    private BigDecimal consumptionTaxRate;
    /**
    * 出口退税率 (%)
    */
    @TableField("export_rebate_rate")
    private BigDecimal exportRebateRate;
    /**
    * 生效日期
    */
    @TableField("effective_date")
    private LocalDateTime effectiveDate;
    /**
    * 失效日期
    */
    @TableField("expire_date")
    private LocalDateTime expireDate;
    /**
    * 是否启用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String HS_CODE = "hs_code";

    public static final String COUNTRY = "country";

    public static final String DESCRIPTION = "description";

    public static final String FIRST_DECLARE_UNIT = "first_declare_unit";

    public static final String SECOND_DECLARE_UNIT = "second_declare_unit";

    public static final String DECLARE_ELEMENT = "declare_element";

    public static final String SUPERVISION_CONDITIONS = "supervision_conditions";

    public static final String MFN_RATE = "mfn_rate";

    public static final String GENERAL_RATE = "general_rate";

    public static final String VAT_RATE = "vat_rate";

    public static final String CONSUMPTION_TAX_RATE = "consumption_tax_rate";

    public static final String EXPORT_REBATE_RATE = "export_rebate_rate";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}