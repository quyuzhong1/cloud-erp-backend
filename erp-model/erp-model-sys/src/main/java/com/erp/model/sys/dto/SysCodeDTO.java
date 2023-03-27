package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

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
     * 类目(编号前缀)
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

    /**
     * 最后更新时间
     */
    private Date updateTime;

    public SysCodeDTO (String category,Integer type) {
        this.category = category;
        this.type = type;
    }

}
