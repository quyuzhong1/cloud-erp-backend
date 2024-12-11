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
    @NotNull(message = "产品尺寸长不能为空")
    private BigDecimal productLength;
    /**
     * 产品尺寸宽
     */
    @NotNull(message = "产品尺寸宽不能为空")
    private BigDecimal productWidth;
    /**
     * 产品尺寸高
     */
    @NotNull(message = "产品尺寸高不能为空")
    private BigDecimal productHeight;

    /**
     * 毛重
     */
    @NotNull(message = "毛重不能为空")
    private BigDecimal grossWeight;

    /**
     * 净重
     */
    @NotNull(message = "净重不能为空")
    private BigDecimal netWeight;

    /**
     * 箱规长
     */
    @NotNull(message = "箱规长不能为空")
    private BigDecimal boxLength;
    /**
     * 箱规宽
     */
    @NotNull(message = "箱规宽不能为空")
    private BigDecimal boxWidth;
    /**
     * 箱规高
     */
    @NotNull(message = "箱规高不能为空")
    private BigDecimal boxHeight;

    /**
     * 单箱重量
     */
    @NotNull(message = "单箱重量不能为空")
    private BigDecimal boxWeight;

    /**
     * 单箱数量
     */
    @NotNull(message = "单箱数量不能为空")
    private BigDecimal boxQty;

}