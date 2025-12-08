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
 * B2C寄样申请单地址信息
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_b2c_application_address")
public class KolB2cApplicationAddressEntity extends BaseEntity<KolB2cApplicationAddressEntity> {

    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 达人ID
    */
    @TableField("partner_id")
    private String partnerId;
    /**
    * 达人昵称
    */
    @TableField("nickname")
    private String nickname;
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
    * 省/州
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
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
    * 收货人
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
    * 收货电话
    */
    @TableField("receiver_phone")
    private String receiverPhone;
    /**
    * 邮编
    */
    @TableField("zip_code")
    private String zipCode;


    public static final String MAIN_ID = "main_id";

    public static final String PARTNER_ID = "partner_id";

    public static final String NICKNAME = "nickname";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT = "district";

    public static final String DETAIL_ADDRESS = "detail_address";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String RECEIVER_PHONE = "receiver_phone";

    public static final String ZIP_CODE = "zip_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}