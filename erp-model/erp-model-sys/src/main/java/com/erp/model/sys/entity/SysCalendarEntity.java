package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author Cloud
 * @since 2023-02-24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_calendar")
@EqualsAndHashCode
public class SysCalendarEntity extends BaseEntity<SysCalendarEntity> {

    /**
     * 日历日期
     */
    @TableField("calendar_date")
    private LocalDate calendarDate;

    /**
     * 是否为工作日
     */
    @TableField("is_work_day")
    private Boolean isWorkDay;

    /**
     * 是否为人工设置
     */
    @TableField("is_manual_set")
    private Boolean isManualSet;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    @TableField("organization")
    private String organization;


    public static final String CALENDAR_DATE = "calendar_date";

    public static final String IS_WORK_DAY = "is_work_day";

    public static final String IS_MANUAL_SET = "is_manual_set";

    public static final String FIELD_REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
