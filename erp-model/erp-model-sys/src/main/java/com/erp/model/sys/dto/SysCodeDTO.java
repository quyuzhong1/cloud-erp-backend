package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:54
 */
@Data
@NoArgsConstructor
public class SysCodeDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 类目
     */
    private String category;

    /**
     * 顺序码
     */
    private Integer num;

    /**
     * 编码类型 (枚举SysNoEnum，1:sku,2:spu)
     */
    private Integer type;

}
