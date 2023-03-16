package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailDTO
 * @Description TODO
 * @Date 2023-03-16 14:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceChangeDetailDTO implements Serializable {


    /**
     * sku id
     */
    private String skuId;


    @NotBlank(message = "采购价目详情表id 不能为空")
    private String purchasePriceDetailId;

    /**
     * sku no
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 采购交期
     */
    private Integer deliveryDate;

    /**
     * 最小数量
     */
    private Integer minQty;

    /**
     * 最大数量
     */
    private Integer maxQty;

    /**
     * 币种
     */
    private String currency;

    /**
     * 币种符号
     */
    private String currencyCode;

    /**
     * 含税单价
     */
    private BigDecimal taxPrice;

    /**
     * 生效时间
     */
    private LocalDate effectiveDate;


    /**
     * 失效时间
     */
    private Date expireDate;

    /**
     * 税率
     */
    private BigDecimal taxRate;

}
