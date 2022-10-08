package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 产品包装信息请求参数
 * @Author: Luo_WG
 * @Date: 2022/9/21 15:46
 **/
@Data
public class ProductPackDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "sku表id 无id：新增 有id：修改")
    private String skuId;

    @ApiModelProperty(value = "产品尺寸")
    private String productSize;

    @ApiModelProperty(value = "毛重")
    private BigDecimal grossWeight;

    @ApiModelProperty(value = "净重")
    private BigDecimal netWeight;

    @ApiModelProperty(value = "箱规")
    private String boxSize;

    @ApiModelProperty(value = "单箱重量")
    private BigDecimal boxWeight;

    @ApiModelProperty(value = "单箱数量")
    private BigDecimal boxQty;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

}