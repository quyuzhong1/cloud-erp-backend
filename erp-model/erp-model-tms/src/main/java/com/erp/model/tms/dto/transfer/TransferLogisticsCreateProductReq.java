package com.erp.model.tms.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 * 备案产品请求DTO
 */
@Data
@AllArgsConstructor
@Builder
public class TransferLogisticsCreateProductReq {

    /**
     * sku编码
     */
    @NotBlank(message = "sku编码不能为空")
    private String sku;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    private String name;

    /**
     * 产品英文名
     */
    @NotNull(message = "产品英文名不能为空")
    private String englishName;

    /**
     * ERP计量单位
     */
    @NotBlank(message = "计量单位不能为空")
    private String unit;

    /**
     * 产品型号
     */
    private String model;

    /**
     * 产品属性
     */
    private String productProperty;

    /**
     * 币别
     */
    @NotBlank(message = "币别不能为空")
    private String currencyCode;

    /**
     * 申报价值
     */
    @NotNull(message = "申报价值不能为空")
    private BigDecimal declaredValue;

    /**
     * 产品重量KG
     */
    @NotNull(message = "产品重量KG不能为空")
    private BigDecimal weight;

    /**
     * 产品长CM
     */
    private BigDecimal length;

    /**
     * 产品宽CM
     */
    private BigDecimal width;

    /**
     * 产品高CM
     */
    private BigDecimal height;

    /**
     * 是否带电池：0否、1是
     */
    @NotNull(message = "是否带电池不能为空")
    private Boolean hasBattery;

    /**
     * 电池类型（hasBattery=1必填）：0不带、1配套电池、2内置电池、
     */
    private Integer batteryType;

    /**
     * 产品海关品名
     */
    @NotNull(message = "产品海关品名不能为空")
    private String hsName;

    /**
     * 海关编码HS_CODE
     */
    @NotBlank(message = "海关编码HS_CODE不能为空")
    private String hsCode;

    /**
     * 申报要素，各个申报要素项使用竖线|分隔
     */
    @NotBlank(message = "申报要素不能为空")
    private String hsElement;

    /**
     * 法定数量（第一数量）
     */
    @NotNull(message = "法定数量（第一数量）不能为空")
    private BigDecimal firstQauntity;

    /**
     * 第二数量，当对应海关编码存在第二单位时必填
     */
    private BigDecimal secondQauntity;
}
