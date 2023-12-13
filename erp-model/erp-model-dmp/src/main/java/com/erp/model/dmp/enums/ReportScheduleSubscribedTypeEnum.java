package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 亚马逊报告计划订阅类型
 * </p>
 *
 * @author Jim
 * @since 2023-11-22
 */
@Getter
@AllArgsConstructor
public enum ReportScheduleSubscribedTypeEnum {

    AMAZON("amazon", "亚马逊报告计划"),
    MANUAL("manual", "手动(定时任务amazonReportJob)"),
    ;

    /**
     * 代号
     */
    @EnumValue
    private final String code;

    /**
     * 名称
     */
    private final String name;


}
