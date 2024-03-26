package com.erp.model.dmp.entity;

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
 * 国家对应的时区配置
 * </p>
 *
 * @author Jim
 * @since 2024-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_timezone")
public class CfgTimezoneEntity extends BaseEntity<CfgTimezoneEntity> {

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 国家代号
     */
    @TableField("country")
    private String country;
    /**
     * 时区
     */
    @TableField("time_zone")
    private String timeZone;
    /**
     * 与UTC相差小时数
     */
    @TableField("utc_diff_hour")
    private Integer utcDiffHour;


    public static final String REMARK = "remark";

    public static final String COUNTRY = "country";

    public static final String TIME_ZONE = "time_zone";

    public static final String UTC_DIFF_HOUR = "utc_diff_hour";

}