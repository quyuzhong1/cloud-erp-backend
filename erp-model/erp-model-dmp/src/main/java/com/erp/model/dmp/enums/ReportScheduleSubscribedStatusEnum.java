package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 亚马逊报告计划取消状态
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Getter
@AllArgsConstructor
public enum ReportScheduleSubscribedStatusEnum {

    NOT("not", "未订阅"),
    WAIT("wait", "待订阅"),
    ALREADY("already", "已订阅"),
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
