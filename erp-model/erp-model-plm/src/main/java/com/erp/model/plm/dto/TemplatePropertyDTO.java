package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yl
 * @Classname TemplatePropertyDTO
 * @Description TODO
 * @Date 2023-03-06 16:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TemplatePropertyDTO implements Serializable {


    /**
     * 模板id
     */
    private  String templateId;


    /**
     * 产品属性id 对应basic_dict 表 id
     */
    private String productPropertyId;

    /**
     * 产品属性id 对应basic_dict 表 value
     */
    private String productPropertyValue;
}
