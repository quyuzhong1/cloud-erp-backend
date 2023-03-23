package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 供应商阶段
 *
 * @author Lambda
 * @Classname SupplierPhaseEnum
 * @Description TODO
 * @Date 2023-03-17 11:56
 * @Created by yl
 */
public enum SupplierPhaseEnum {

    POTENTIAL("potential", "潜在", 0),
    ACCESS("access", "准入", 1),
    CONFORM("conform", "合格", 2),
    ELIMINATE("eliminate", "淘汰",3);


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
}
