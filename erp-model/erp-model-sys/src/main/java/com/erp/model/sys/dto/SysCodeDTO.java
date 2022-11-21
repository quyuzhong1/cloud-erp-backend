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
     * 颜色编码
     */
    private String colorCode;

    /**
     * 销售渠道
     */
    private String saleChannel;

    /**
     * 迭代版本
     */
    private String version;

    /**
     * 客户定制
     */
    private String customized;

    /**
     * 编码类型
     */
    private Integer type;

}
