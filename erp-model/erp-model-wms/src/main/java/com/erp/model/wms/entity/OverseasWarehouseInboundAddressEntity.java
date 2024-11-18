package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 海外入库单常用揽收地址
 * </p>
 *
 * @author Jim
 * @since 2023-12-04
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("overseas_warehouse_inbound_address")
public class OverseasWarehouseInboundAddressEntity extends BaseEntity<OverseasWarehouseInboundAddressEntity> {

    /**
     * 平台类型: iml=艾姆勒
     */
    @TableField("dict_platform")
    private String dictPlatform;

    /**
     * 省ID
     */
    @TableField("dict_province_id")
    private String dictProvinceId;

    /**
     * 城市ID
     */
    @TableField("dict_city_id")
    private String dictCityId;

    /**
     * 地区ID
     */
    @TableField("dict_district_id")
    private String dictDistrictId;

    /**
     * 性
     */
    @TableField("first_name")
    private String firstName;

    /**
     * 名
     */
    @TableField("last_name")
    private String lastName;

    /**
     * 手机
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 详情
     */
    @TableField("street")
    private String street;

    /**
     * 地址邮编
     */
    @TableField("zipcode")
    private String zipcode;


    public static final String DICT_PLATFORM = "dict_platform";

    public static final String DICT_PROVINCE_ID = "dict_province_id";

    public static final String DICT_CITY_ID = "dict_city_id";

    public static final String DICT_DISTRICT_ID = "dict_district_id";

    public static final String FIRST_NAME = "first_name";

    public static final String LAST_NAME = "last_name";

    /**
     * 所有ID
     */
    public List<String> getAllDictCityId() {
        return Arrays.asList(this.dictDistrictId, this.dictCityId, this.dictDistrictId);
    }
}
