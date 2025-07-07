package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p>
 * dmp输入任务执行系统
 * </p>
 *
 * @author Jim
 * @since 2025-07-07
 */
@Getter
@AllArgsConstructor
public enum DmpCfgInputExecSystemEnum {

    DMP("dmp", "ERP中台系统"),
    REST_CLOUD("restCloud","restCloud系统"),
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

    public static DmpCfgInputExecSystemEnum getByCode(String code) {
        return Arrays.stream(DmpCfgInputExecSystemEnum.values())
                .filter(r -> r.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }

}
