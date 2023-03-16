package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 供应商等级
 * @author Lambda
 * @Classname SupplierGradeDTO
 * @Description TODO
 * @Date 2023-03-16 14:47
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierGradeDTO implements Serializable {


    /**
     * 表id
     */
    private String id;


    /**
     * 名称
     */
    private String name;
}
