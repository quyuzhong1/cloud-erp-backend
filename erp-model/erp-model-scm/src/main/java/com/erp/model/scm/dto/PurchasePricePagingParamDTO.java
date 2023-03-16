package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购价目 分页入参
 *
 * @author Lambda
 * @Classname PurchasePricePagingParamDTO
 * @Description TODO
 * @Date 2023-03-16 15:32
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePricePagingParamDTO implements Serializable {


    /**
     * 产品名称
     */
    private String productName;


    /**
     * sku id 集合
     */
    private List<String> skuIdList;


    /**
     * 采购组织id集合
     */
    private List<String> purchaseOrgIdList;


    /**
     * 单据状态
     */
    private List<String> approveStatusList;

    /**
     * 生效开始时间
     */
    private LocalDate effectiveDateBegin;

    /**
     * 生效结束时间
     */
    private LocalDate effectiveDateEnd;

    /**
     * 失效开始时间
     */
    private LocalDate expireDateBegin;

    /**
     * 失效结束时间
     */
    private LocalDate expireDateEnd;


    /**
     * 创建人id 集合
     */
    private List<String> createUserIdList;


    /**
     * 创建开始时间
     */
    private LocalDate createTimeBegin;


    /**
     * 创建结束时间
     */
    private LocalDate createTimeEnd;

}
