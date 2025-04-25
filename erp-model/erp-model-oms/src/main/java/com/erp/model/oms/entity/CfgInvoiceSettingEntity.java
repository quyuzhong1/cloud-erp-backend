package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_invoice_setting")
public class CfgInvoiceSettingEntity extends BaseEntity<CfgInvoiceSettingEntity> {

    /**
    * 发票类型
    */
    @TableField("type")
    private String type;
    /**
    * 公司名称
    */
    @TableField("company_name")
    private String companyName;
    /**
    * 法人国家经济号
    */
    @TableField("lei_code")
    private String leiCode;
    /**
    * 启用禁用 true禁用 false启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 税务类型
    */
    @TableField("tax_type")
    private String taxType;
    /**
    * 公司类型
    */
    @TableField("dict_company_type")
    private String dictCompanyType;
    /**
    * 州税号
    */
    @TableField("state_tax_no")
    private String stateTaxNo;
    /**
    * 邮箱
    */
    @TableField("email")
    private String email;
    /**
    * 邮编
    */
    @TableField("post_code")
    private String postCode;
    /**
    * 地址
    */
    @TableField("address")
    private String address;
    /**
    * 门牌号
    */
    @TableField("doorplate_no")
    private String doorplateNo;
    /**
    * 区
    */
    @TableField("district")
    private String district;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 州
    */
    @TableField("state")
    private String state;
    /**
    * 序列号
    */
    @TableField("no")
    private Integer no;
    /**
    * 起始编号
    */
    @TableField("start_code")
    private String startCode;
    /**
    * A1证书链接
    */
    @TableField("certificate_url")
    private String certificateUrl;
    /**
    * A1证书密码
    */
    @TableField("certificate_password")
    private String certificatePassword;
    /**
     * 接口返回
     */
    @TableField("token")
    private String token;

    public static final String TYPE = "type";

    public static final String COMPANY_NAME = "company_name";

    public static final String LEI_CODE = "lei_code";

    public static final String DISABLED = "disabled";

    public static final String TAX_TYPE = "tax_type";

    public static final String DICT_COMPANY_TYPE = "dict_company_type";

    public static final String STATE_TAX_NO = "state_tax_no";

    public static final String EMAIL = "email";

    public static final String POST_CODE = "post_code";

    public static final String ADDRESS = "address";

    public static final String DOORPLATE_NO = "doorplate_no";

    public static final String DISTRICT = "district";

    public static final String CITY = "city";

    public static final String STATE = "state";

    public static final String NO = "no";

    public static final String START_CODE = "start_code";

    public static final String CERTIFICATE_URL = "certificate_url";

    public static final String CERTIFICATE_PASSWORD = "certificate_password";

    @Override
    public Serializable pkVal() {
        return null;
    }

}