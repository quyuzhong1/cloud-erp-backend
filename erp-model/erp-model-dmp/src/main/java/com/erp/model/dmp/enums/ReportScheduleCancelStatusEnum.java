package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 亚马逊报告计划订阅状态
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Getter
@AllArgsConstructor
public enum ReportScheduleCancelStatusEnum {

    NONE("none", "未取消"),
    WAIT("wait", "待取消"),
    ALREADY("already", "已取消"),
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
