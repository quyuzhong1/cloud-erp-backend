package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* @Description 产品成本信息表
* @Author Luo_WG
* @Date 2022/9/22 16:03
**/
@Data
@NoArgsConstructor
public class ProductCostDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "sku表id 无id：新增 有id：修改")
    private String skuId;

    @NotNull(message = "目标含税成本不能为空")
    @ApiModelProperty(value = "目标含税成本", required = true)
    private BigDecimal targetTaxCost;

    @NotNull(message = "目标不含税成本不能为空")
    @ApiModelProperty(value = "目标不含税成本", required = true)
    private BigDecimal targetNoTaxCost;

    @NotNull(message = "实际含税成本不能为空")
    @ApiModelProperty(value = "实际含税成本", required = true)
    private BigDecimal actualTaxCost;

    @NotNull(message = "实际不含税成本不能为空")
    @ApiModelProperty(value = "实际不含税成本", required = true)
    private BigDecimal actualNoTaxCost;

    @NotNull(message = "标准零售价不能为空")
    @ApiModelProperty(value = "标准零售价", required = true)
    private BigDecimal retailPrice;

    @NotNull(message = "目标毛利率不能为空")
    @ApiModelProperty(value = "目标毛利率", required = true)
    private BigDecimal targetGpm;

    @NotNull(message = "实际毛利率（人民币）不能为空")
    @ApiModelProperty(value = "实际毛利率（人民币）", required = true)
    private BigDecimal actualGpmCny;

    @NotNull(message = "实际毛利率（美元）不能为空")
    @ApiModelProperty(value = "实际毛利率（美元）", required = true)
    private BigDecimal actualGpmUsd;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;
}