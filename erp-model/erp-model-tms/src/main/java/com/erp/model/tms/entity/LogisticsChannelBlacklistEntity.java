package com.erp.model.tms.entity;

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
    @TableField("district ")
    private String district ;


    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String COUNTRY = "country";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT  = "district ";

    @Override
    public Serializable pkVal() {
        return null;
    }

}