package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Lambda
 * @Classname PurchasePricePagingViewDTO
 * @Description TODO
 * @Date 2023-03-16 15:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePricePagingViewDTO  implements Serializable {


    /**
     * 表id
     */
    private String id;


    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 供应商名
     */
    private String supplierName;

    /**
     * sku id
     */
    private String skuId;


    /**
     * sku no
     */
    private String skuNo;


    /**
     * 产品名称
     */
    private String productName;

    /**
     * 最小数量
     */
    private Integer minQty;

    /**
     * 最大数量
     */
    private Integer maxQty;

    /**
     * 含税单价
     */
    private BigDecimal taxPrice;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 生效时间
     */
    private LocalDate effectiveDate;


    /**
     * 失效时间
     */
    private Date expireDate;

    /**
     * 采购组织
     */
    private String purchaseOrgId;

    /**
     * 采购组织名
     */
    private String purchaseOrgName;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
