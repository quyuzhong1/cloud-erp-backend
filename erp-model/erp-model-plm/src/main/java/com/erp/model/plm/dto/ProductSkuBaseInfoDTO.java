package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ProductSkuBaseInfoDTO {

    /**
     * 产品sku表id 无id：新增 有id：修改
     */
    @ApiModelProperty(value = "产品sku表id 无id：新增 有id：修改")
    private String id;

    /**
     * 产品信息表主id
     */
    @ApiModelProperty(value = "产品信息表主id", required = true)
    private String productId;

    /**
     * skuNo
     */
    @ApiModelProperty(value = "skuNo",required = true)
    private String skuNo;

    /**
     * 计划上市时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    @ApiModelProperty(value = "计划上市时间")
    private Date planListedTime;

    /**
     * 单位表id
     */
    @ApiModelProperty(value = "单位表id")
    private String unitId;

    /**
     * 单位名称
     */
    @ApiModelProperty(value = "单位名称")
    private String unitName;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "产品状态错误")
    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发",required = true)
    private Integer productState;

    /**
     * 产品图片
     */
    @ApiModelProperty(value = "产品图片")
    private String imagesUrl;

    /**
     * 产品负责人id
     */
    @ApiModelProperty(value = "产品负责人id")
    private String chargeId;

    /**
     * 产品负责人名称
     */
    @ApiModelProperty(value = "产品负责人名称")
    private String chargeName;

}
