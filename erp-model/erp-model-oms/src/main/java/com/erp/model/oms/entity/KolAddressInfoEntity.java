package com.erp.model.oms.entity;

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
 * 达人地址信息
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_address_info")
public class KolAddressInfoEntity extends BaseEntity<KolAddressInfoEntity> {

    /**
    * main_id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 国家id
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 国家
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 省/州id
    */
    @TableField("province_id")
    private String provinceId;
    /**
    * 省/州
    */
    @TableField("province_name")
    private String provinceName;
    /**
    * 城市id
    */
    @TableField("city_id")
    private String cityId;
    /**
    * 城市
    */
    @TableField("city_name")
    private String cityName;
    /**
    * 区域
    */
    @TableField("district")
    private String district;
    /**
    * 详细地址
    */
    @TableField("detail_address")
    private String detailAddress;
    /**
    * 联系人
    */
    @TableField("contact_person")
    private String contactPerson;
    /**
    * 邮箱
    */
    @TableField("email")
    private String email;
    /**
    * 联系电话
    */
    @TableField("phone")
    private String phone;
    /**
    * 邮编
    */
    @TableField("zip_code")
    private String zipCode;
    /**
    * 是否默认地址
    */
    @TableField("is_default")
    private Boolean isDefault;
    /**
    * 地址备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 是否启用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String MAIN_ID = "main_id";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String PROVINCE_ID = "province_id";

    public static final String PROVINCE_NAME = "province_name";

    public static final String CITY_ID = "city_id";

    public static final String CITY_NAME = "city_name";

    public static final String DISTRICT = "district";

    public static final String DETAIL_ADDRESS = "detail_address";

    public static final String CONTACT_PERSON = "contact_person";

    public static final String EMAIL = "email";

    public static final String PHONE = "phone";

    public static final String ZIP_CODE = "zip_code";

    public static final String IS_DEFAULT = "is_default";

    public static final String REMARK = "remark";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}