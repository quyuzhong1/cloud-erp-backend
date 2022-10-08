package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:06
 **/
@Data
@NoArgsConstructor
public class ProductLogisticsDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "sku表id 无id：新增 有id：修改")
    private String skuId;

    @ApiModelProperty(value = "产品属性")
    private String productProperty;

    @ApiModelProperty(value = "产品属性id")
    private String productPropertyId;

    @ApiModelProperty(value = "报关中文名")
    private String declareChineseName;

    @ApiModelProperty(value = "报关英文名")
    private String declareEnglishName;

    @ApiModelProperty(value = "报关申报价格")
    private BigDecimal declarePrice;

    @ApiModelProperty(value = "海关编码")
    private String customsCode;

    @ApiModelProperty(value = "申报要素")
    private String declareElement;

    @ApiModelProperty(value = "英文材质")
    private String englishMaterial;

    @ApiModelProperty(value = "英文用途")
    private String englishUsage;

    private static final long serialVersionUID = 1L;
}