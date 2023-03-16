package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 供应商阶段分页展示数据
 * @author Lambda
 * @Classname SupplierPhasePagingViemDTO
 * @Description TODO
 * @Date 2023-03-16 12:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPhasePagingViewDTO implements Serializable {

    /**
     * 供应商 阶段表 id
     */
    private String id;


    /**
     * 供应商表id
     */
    private String SupplierId;
}
