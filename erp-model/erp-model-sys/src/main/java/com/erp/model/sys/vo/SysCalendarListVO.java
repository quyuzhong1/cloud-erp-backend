package com.erp.model.sys.vo;

import com.erp.model.sys.entity.SysCalendarEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author cloud
 */
@Data
@NoArgsConstructor
public class SysCalendarListVO {

    private String id;

    /**
     * 日期
     */
    private LocalDate calendarDate;

    /**
     * 是否工作日
     */
    private Boolean isWorkDay;

    /**
     * 备注
     */
    private String remark;

    public SysCalendarListVO(SysCalendarEntity entity) {
        this.id = entity.getId();
        this.calendarDate = entity.getCalendarDate();
        this.isWorkDay = entity.getIsWorkDay();
        this.remark = entity.getRemark();
    }

}