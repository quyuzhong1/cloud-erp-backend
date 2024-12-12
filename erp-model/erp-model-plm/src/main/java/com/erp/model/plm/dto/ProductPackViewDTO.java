package com.erp.model.plm.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品包装信息回显
 **/
@Data
public class ProductPackViewDTO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * sku编号
     */
    private String skuNo;
    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品尺寸长
     */
    private BigDecimal productLength;
    /**
     * 产品尺寸宽
     */
    private BigDecimal productWidth;
    /**
     * 产品尺寸高
     */
    private BigDecimal productHeight;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 箱规长
     */
    private BigDecimal boxLength;
    /**
     * 箱规宽
     */
    private BigDecimal boxWidth;
    /**
     * 箱规高
     */
    private BigDecimal boxHeight;

    /**
     * 单箱重量
     */
    private BigDecimal boxWeight;

    /**
     * 单箱数量
     */
    private BigDecimal boxQty;

}