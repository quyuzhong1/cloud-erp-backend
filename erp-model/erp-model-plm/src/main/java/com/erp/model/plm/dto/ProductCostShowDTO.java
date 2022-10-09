package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* @Description 产品成本明细查询列表返回值（VO）
* @Author Luo_WG
* @Date 2022/9/22 16:03
**/
@Data
@NoArgsConstructor
public class ProductCostShowDTO implements Serializable {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * 产品sku表id
     */
    @ApiModelProperty(value = "产品sku表id")
    private String skuId;

    /**
     * 产品sku图片
     */
    @ApiModelProperty(value = "产品sku图片")
    private String imagesUrl;

    /**
     * skuNo
     */
    @ApiModelProperty(value = "skuNo")
    private String skuNo;

    /**
     * 目标含税成本
     */
    @ApiModelProperty(value = "目标含税成本")
    private BigDecimal targetTaxCost;

    /**
     * 目标不含税成本
     */
    @ApiModelProperty(value = "目标不含税成本")
    private BigDecimal targetNoTaxCost;

    /**
     * 实际含税成本
     */
    @ApiModelProperty(value = "实际含税成本")
    private BigDecimal actualTaxCost;

    /**
     * 实际不含税成本
     */
    @ApiModelProperty(value = "实际不含税成本")
    private BigDecimal actualNoTaxCost;

    /**
     * 标准零售价
     */
    @ApiModelProperty(value = "标准零售价")
    private BigDecimal retailPrice;

    /**
     * 目标毛利率
     */
    @ApiModelProperty(value = "目标毛利率")
    private BigDecimal targetGpm;

    /**
     * 实际毛利率（人民币）
     */
    @ApiModelProperty(value = "实际毛利率（人民币）")
    private BigDecimal actualGpmCny;

    /**
     * 实际毛利率（美元）
     */
    @ApiModelProperty(value = "实际毛利率（美元）")
    private BigDecimal actualGpmUsd;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    /**
     * 修改人id
     */
    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    /**
     * 修改人名称
     */
    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;

    private static final long serialVersionUID = 1L;
}