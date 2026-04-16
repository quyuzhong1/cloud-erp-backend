package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
* @Description 产品成本明细查询列表返回值（VO）
* @Author Luo_WG
* @Date 2022/9/22 16:03
**/
@Data
@NoArgsConstructor
public class ProductRetailPriceShowDTO implements Serializable {

    /**
     * 产品sku图片
     */
    private String imagesUrl;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 币别
     */
     private String currency;

     /**
     * 标准零售价(含税)
     */
     private BigDecimal stdRetailPriceVat;

     /**
     * 税率
     */
     private BigDecimal vatRate;
     
     /**
      * 税率，带%的
      */
     private String vatRateStr;

     /**
     * 标准零售价(不含税)
     */
     private BigDecimal stdRetailPrice;
    
    

    private static final long serialVersionUID = 1L;
}