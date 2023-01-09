package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 我的仪表盘
 *
 * @Classname DashboardDTO
 * @Description TODO
 * @Date 2022-12-09 9:40
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DashboardDTO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * name
     */
    private String name;

    /**
     * 是否默认 true是 false 不是
     */
    private Boolean isDefault=false;


    /**
     * 是否常用 1 是  0  不不是
     */
    private Integer isFrequently;


}
