package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname SupplierPhaseDTO
 * @Description TODO
 * @Date 2023-03-16 12:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPhaseDTO implements Serializable {


    /**
     * 表id
     */
    private String id;


    /**
     * 供应商表id
     */
    private String supplierId;

    /**
     * 操作类型
     */
    private String type;


    /**
     * 当前阶段
     */
    private String currentPhase;

    /**
     * 目标阶段
     */
    private String targetPhase;

    /**
     * 说明
     */
    private String description;
}
