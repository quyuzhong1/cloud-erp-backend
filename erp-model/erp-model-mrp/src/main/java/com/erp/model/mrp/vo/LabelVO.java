package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelVO {

    private String replenishmentId;
    /**
     * 名字
     */
    private String name;
    /**
     * 颜色
     */
    private String color;
}
