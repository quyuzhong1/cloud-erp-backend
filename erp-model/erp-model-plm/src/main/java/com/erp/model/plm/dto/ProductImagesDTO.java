package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @Description 产品图片信息请求参数
 * @Author Luo_WG
 * @Date 2022/9/26 16:38
 **/
@Data
@NoArgsConstructor
public class ProductImagesDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "产品表id")
    private String productId;

    @ApiModelProperty(value = "产品sku明细表id")
    private String skuId;

    @ApiModelProperty(value = "图片地址")
    private String imagesUrl;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    private static final long serialVersionUID = 1L;
}