package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p>
 * 亚马逊报告创建方式
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Getter
@AllArgsConstructor
public enum AmzReportCreatedMethodEnum {

    SYSTEM("system", "ERP系统自动请求创建", ReportScheduleSubscribedTypeEnum.MANUAL),
    QUERY("query","查询亚马逊系统创建", ReportScheduleSubscribedTypeEnum.QUERY),
    MANUAL("manual","ERP系统人工请求创建", null),
    NONE("none","未知", null),

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

    private final ReportScheduleSubscribedTypeEnum subscribedType;

    /**
     * 根据订阅类型匹配创建方式
     */
    public static AmzReportCreatedMethodEnum getBySubscribedType(String subscribedType) {
        ReportScheduleSubscribedTypeEnum subscribedTypeEnum = ReportScheduleSubscribedTypeEnum.getByCode(subscribedType);
        if (null == subscribedTypeEnum){
            return NONE;
        }
        return Arrays.stream(AmzReportCreatedMethodEnum.values())
                .filter(e-> e.getSubscribedType().equals(subscribedTypeEnum))
                .findFirst()
                .orElse(NONE);
    }
}
