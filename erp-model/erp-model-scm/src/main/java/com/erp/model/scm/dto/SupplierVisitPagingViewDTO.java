package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Lambda
 * @Classname SupplierVisitPagingViewDTO
 * @Description TODO
 * @Date 2023-03-16 14:40
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierVisitPagingViewDTO implements Serializable {

    /**
     * 表id
     */
    private String id;


    /**
     * 拜访时间
     */
    private LocalDate visitTime;

    /**
     * 拜访人
     */
    private String people;


    /**
     * 物料信息
     */
    private String skuInfo;

    /**
     * 结果
     */
    private String result;

    /**
     * 内容
     */
    private String content;
}
