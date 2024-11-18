package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


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
    private LogisticsAddressTypeEnum type;
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
    * 国家
    */
    @TableField("country_name")
    private String countryName;



    /**
    * 省
    */
    @TableField("province_name")
    private String provinceName;
    /**
    * 城市
    */
    @TableField("city_name")
    private String cityName;


    /**
    * 区
    */
    @TableField("district_name")
    private String districtName;


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
    /**
     * 街道
     */
    @TableField("street")
    private String street;
    /**
     *传真
     */
    @TableField("fax")
    private String fax;
    /**
     *交易管理id
     */
    @TableField("trade_manage_id")
    private String tradeManageId;
    /**
     *源地址id
     */
    @TableField("address_id")
    private String addressId;
    /**
     *是否默认 false 不是默认
     */
    @TableField("is_default")
    private int isDefault;
    /**
     *语言 默认CN
     */
    @TableField("language")
    private String language;
    /**
     * 数据同步方式
     */
    @TableField("is_by_sync")
    private Boolean isBySync;
     /**
     * 店铺id shop_id
     */
     @TableField("shop_id")
    private String shopId;


    public static final String FIELD_NAME = "name";

    public static final String FIELD_TYPE = "type";

    public static final String COMPANY_NAME = "company_name";

    public static final String FIELD_CONTACT = "contact";

    public static final String FIELD_EMAIL = "email";

    public static final String TEL_NUMBER = "tel_number";

    public static final String FIELD_COUNTRY = "country";


    public static final String ADDRESS_FIRST = "address_first";

    public static final String ADDRESS_SECOND = "address_second";

    public static final String ZIP_CODE = "zip_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}