package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

/**
* @Description 产品成本信息表
* @Author Luo_WG
* @Date 2022/9/22 16:03
**/
@Data
@NoArgsConstructor
public class ProductCostDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * 目标含税成本
     */
    @Digits(integer = 20,fraction = 4,message = "目标含税成本最大20字符，小数位不能大于4个字符")
    private BigDecimal targetTaxCost;

    /**
     * 目标不含税成本
     */
    @Digits(integer = 20,fraction = 4,message = "目标不含税成本最大20字符，小数位不能大于4个字符")
    private BigDecimal targetNoTaxCost;

    /**
     * 实际含税成本
     */
    @Digits(integer = 20,fraction = 4,message = "实际含税成本最大20字符，小数位不能大于4个字符")
    private BigDecimal actualTaxCost;

    /**
     * 实际不含税成本
     */
    @Digits(integer = 20,fraction = 4,message = "实际不含税成本最大20字符，小数位不能大于4个字符")
    private BigDecimal actualNoTaxCost;

    /**
     * 标准零售价
     */
    @Digits(integer = 20,fraction = 4,message = "标准零售价最大20字符，小数位不能大于4个字符")
    private BigDecimal retailPrice;

    /**
     * 目标毛利率
     */
    @Digits(integer = 20,fraction = 4,message = "目标毛利率最大20字符，小数位不能大于4个字符")
    private BigDecimal targetGpm;

    /**
     * 实际毛利率（人民币）
     */
    @Digits(integer = 20,fraction = 4,message = "实际毛利率（人民币）最大20字符，小数位不能大于4个字符")
    private BigDecimal actualGpmCny;

    /**
     * 实际毛利率（美元）
     */
    @Digits(integer = 20,fraction = 4,message = "实际毛利率（美元）最大20字符，小数位不能大于4个字符")
    private BigDecimal actualGpmUsd;

    /**
     * 立项成本
     */
    @Digits(integer = 20,fraction = 4,message = "立项成本最大20字符，小数位不能大于4个字符")
    private BigDecimal projectApprovalCost;

    /**
     * 量产成本
     */
    @Digits(integer = 20,fraction = 4,message = "量产成本最大20字符，小数位不能大于4个字符")
    private BigDecimal massCost;

    /**
     * 项目成本
     */
    @Digits(integer = 20,fraction = 4,message = "项目成本最大20字符，小数位不能大于4个字符")
    private BigDecimal projectCost;


    /**
     *税率
     */
    @Digits(integer = 20,fraction = 4,message = "税率最大20字符，小数位不能大于4个字符")
    private BigDecimal taxRate;
}