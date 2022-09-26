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

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "产品sku表id")
    private String skuId;

    @ApiModelProperty(value = "产品sku图片")
    private String imagesUrl;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "目标含税成本")
    private BigDecimal targetTaxCost;

    @ApiModelProperty(value = "目标不含税成本")
    private BigDecimal targetNoTaxCost;

    @ApiModelProperty(value = "实际含税成本")
    private BigDecimal actualTaxCost;

    @ApiModelProperty(value = "实际不含税成本")
    private BigDecimal actualNoTaxCost;

    @ApiModelProperty(value = "标准零售价")
    private BigDecimal retailPrice;

    @ApiModelProperty(value = "目标毛利率")
    private BigDecimal targetGpm;

    @ApiModelProperty(value = "实际毛利率（人民币）")
    private BigDecimal actualGpmCny;

    @ApiModelProperty(value = "实际毛利率（美元）")
    private BigDecimal actualGpmUsd;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}