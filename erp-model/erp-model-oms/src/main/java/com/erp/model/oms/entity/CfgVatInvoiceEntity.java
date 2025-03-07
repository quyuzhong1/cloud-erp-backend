package com.erp.model.oms.entity;

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
 * VAT发票设置
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_vat_invoice")
public class CfgVatInvoiceEntity extends BaseEntity<CfgVatInvoiceEntity> {

    /**
    * 禁用状态 false 启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺国家id
    */
    @TableField("shop_country_code")
    private String shopCountryCode;
    /**
    * 启用时间
    */
    @TableField("enable_time")
    private LocalDateTime enableTime;
    /**
    * 自动上传（默认是）
    */
    @TableField("is_auto_upload")
    private Boolean isAutoUpload;
    /**
    * VAT税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 公司名称
    */
    @TableField("company_name")
    private String companyName;
    /**
    * 详细地址+城市+州/省+邮编+国家
    */
    @TableField("company_address")
    private String companyAddress;
    /**
    * 国家二字码
    */
    @TableField("country_code")
    private String countryCode;
    /**
    * 州/省
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 邮编
    */
    @TableField("post_code")
    private String postCode;
    /**
    * 详细地址
    */
    @TableField("address")
    private String address;
    /**
    * 税号
    */
    @TableField("vat_no")
    private String vatNo;
    /**
    * 模板类型:erp=ERP模板,official=官方模板  枚举：CfgVatInvoiceTemplateTypeEnum
    */
    @TableField("template_type")
    private String templateType;


    public static final String DISABLED = "disabled";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_COUNTRY_CODE = "shop_country_code";

    public static final String ENABLE_TIME = "enable_time";

    public static final String IS_AUTO_UPLOAD = "is_auto_upload";

    public static final String TAX_RATE = "tax_rate";

    public static final String COMPANY_NAME = "company_name";

    public static final String COMPANY_ADDRESS = "company_address";

    public static final String COUNTRY_CODE = "country_code";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String POST_CODE = "post_code";

    public static final String ADDRESS = "address";

    public static final String VAT_NO = "vat_no";

    public static final String TEMPLATE_TYPE = "template_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
