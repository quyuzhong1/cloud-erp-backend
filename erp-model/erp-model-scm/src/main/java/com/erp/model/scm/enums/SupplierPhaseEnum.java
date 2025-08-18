package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.Arrays;

/**
 * 供应商阶段
 *
 * @author Lambda
 * @Classname SupplierPhaseEnum

 * @Date 2023-03-17 11:56
 * @Created by yl
 */
public enum SupplierPhaseEnum {

    STRATEGY("strategy", "战略", 0),
    PREFERRED("preferred", "优选", 1),
    QUALIFIED("qualified", "合格", 2),
    PREELIMINATION("preElimination", "预淘汰", 3),
    ELIMINATED("eliminated", "已淘汰", 4),
    ;


    @EnumValue
    private String phase;
    private String name;
    private Integer seq;

    SupplierPhaseEnum(String phase, String name, Integer seq) {
        this.phase = phase;
        this.name = name;
        this.seq = seq;
    }

    public String getPhase() {
        return phase;
    }

    public String getName() {
        return name;
    }

    public static String getPhaseName(String phase) {
        SupplierPhaseEnum phaseEnum = Arrays.stream(values()).filter(p -> p.getPhase().equals(phase))
                .findFirst().orElse(null);
        if (phaseEnum != null) {
            return phaseEnum.getName();
        }
        return "";

    }


    public static SupplierPhaseEnum getPhase(String phase) {
        SupplierPhaseEnum phaseEnum = Arrays.stream(values()).filter(p -> p.getPhase().equals(phase))
                .findFirst().orElse(null);
        return phaseEnum;

    }
}
