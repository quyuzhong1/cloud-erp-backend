package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname DictBasicDTO
 * @Description TODO
 * @Date 2023-03-16 16:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {


    /**
     * 表id
     */
    private String id;




    /**
     * remark
     */
    private String remark;

    /**
     * value 使用值
     */
    private String value;

    /**
     * type 属性
     * 查询依据
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 序号
     */
    private Integer sort;
}
