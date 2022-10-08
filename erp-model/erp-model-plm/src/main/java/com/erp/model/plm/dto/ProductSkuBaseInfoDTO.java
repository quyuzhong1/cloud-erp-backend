package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ProductSkuBaseInfoDTO {
    @ApiModelProperty(value = "产品sku表id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "产品信息表主id", required = true)
    private String productId;

    @ApiModelProperty(value = "skuNo",required = true)
    private String skuNo;

    @ApiModelProperty(value = "计划上市时间")
    private String planListedTime;

    @ApiModelProperty(value = "单位表id")
    private String unitId;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "产品状态错误")
    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发",required = true)
    private Integer productState;

    @ApiModelProperty(value = "产品图片")
    private String imagesUrl;

    @ApiModelProperty(value = "产品负责人id")
    private String charge_id;

    @ApiModelProperty(value = "产品负责人名称")
    private String charge_name;

}
