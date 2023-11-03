package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.erp.model.tms.enums.LogisticsAddressTypeEnums;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 物流地址表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_address")
public class LogisticsAddressEntity extends BaseEntity<LogisticsAddressEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 类型
    */
    @TableField("type")
    private LogisticsAddressTypeEnums type;
    /**
    * 公司名
    */
    @TableField("company_name")
    private String companyName;
    /**
    * 联系人
    */
    @TableField("contact")
    private String contact;
    /**
    * 邮箱
    */
    @TableField("email")
    private String email;
    /**
    * 电话
    */
    @TableField("tel_number")
    private String telNumber;
    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 省
    */
    @TableField("province_id")
    private String provinceId;
    /**
    * 城市
    */
    @TableField("city_id")
    private String cityId;
    /**
    * 区
    */
    @TableField("district_id")
    private String districtId;
    /**
    * 详细地址1
    */
    @TableField("address_first")
    private String addressFirst;
    /**
    * 详细地址2
    */
    @TableField("address_second")
    private String addressSecond;
    /**
    * 邮编
    */
    @TableField("zip_code")
    private String zipCode;


    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String COMPANY_NAME = "company_name";

    public static final String CONTACT = "contact";

    public static final String EMAIL = "email";

    public static final String TEL_NUMBER = "tel_number";

    public static final String COUNTRY = "country";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT  = "district ";

    public static final String ADDRESS_FIRST = "address_first";

    public static final String ADDRESS_SECOND = "address_second";

    public static final String ZIP_CODE = "zip_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}