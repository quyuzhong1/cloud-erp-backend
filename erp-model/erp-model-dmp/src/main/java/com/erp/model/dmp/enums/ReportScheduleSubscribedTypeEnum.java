package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

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
    QUERY("query", "查询最新"),
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


    public static ReportScheduleSubscribedTypeEnum getByCode(String code){
        return Arrays.stream(ReportScheduleSubscribedTypeEnum.values())
                .filter(e-> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
