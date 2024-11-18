package com.erp.model.dmp.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


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
    /**
     * 条件:亚马逊=销售渠道（多个，拼接）
     */
    @TableField("condition")
    private String condition;


    public static final String REMARK = "remark";

    public static final String COUNTRY = "country";

    public static final String TIME_ZONE = "time_zone";

    public static final String UTC_DIFF_HOUR = "utc_diff_hour";

    public List<String> getAndParseCondition(){
        if (StringUtils.isBlank(this.condition)){
            return Collections.emptyList();
        }
        if (this.condition.contains(",")){
            return Arrays.stream(this.condition.split(",")).collect(Collectors.toList());
        } else {
            return Collections.singletonList(this.condition);
        }
    }

}