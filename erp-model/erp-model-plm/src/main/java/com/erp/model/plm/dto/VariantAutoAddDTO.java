package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 商品管理-产品信息-多规格-自动生成 请求参数
 * @Author Luo_WG
 * @Date 2022/9/26 14:37
 **/
@Data
@NoArgsConstructor
public class VariantAutoAddDTO {

    @ApiModelProperty(value = "产品信息表id",required = true)
    private String productId;

    @ApiModelProperty(value = "产品名称(款名)",required = true)
    private String productName;

    @ApiModelProperty(value = "变体属性",required = true)
    private List<VarianRefPropertyDTO> varianRefPropertyList;
}
