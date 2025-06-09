package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 渠道黑名单表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_channel_blacklist")
public class LogisticsChannelBlacklistEntity extends BaseEntity<LogisticsChannelBlacklistEntity> {

    /**
    * 渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 省 州
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 区
    */
    @TableField("district")
    private String district;


    /**
     * 国家
     */
    @TableField("country_name")
    private String countryName;
    /**
     * 省 州
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


    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String FIELD_COUNTRY = "country";

    public static final String FIELD_PROVINCE = "province";

    public static final String FIELD_CITY = "city";

    public static final String FIELD_DISTRICT  = "district ";

    @Override
    public Serializable pkVal() {
        return null;
    }

}