package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
* @Description 产品明细查询列表返回值（VO）
* @Author Luo_WG
* @Date 2022/9/22 10:40
**/
@Data
@NoArgsConstructor
public class ProductDetailShowDTO implements Serializable {
    @ApiModelProperty(value = "产品sku信息表id")
    private String id;

    @ApiModelProperty(value = "图片")
    private String imagesUrl;

    @ApiModelProperty(value = "spuNo")
    private String spuNo;

    @ApiModelProperty(value = "skuNo")
    private String skuNo;

    @ApiModelProperty(value = "产品名称")
    private String name;

    @ApiModelProperty(value = "首批到货量")
    private String actualArrivalNum;

    @ApiModelProperty(value = "首批到货状态：1.未到货 2.已到货 3.部分到货")
    private Date arrivalState;

    @ApiModelProperty(value = "侵权风险")
    private String pirateRisk;

    @ApiModelProperty(value = "标准零售价")
    private Integer retailPrice;

    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发")
    private String productState;

    @ApiModelProperty(value = "创建人")
    private String createUserId;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "规格类型  1：无规格  2：多规格")
    private String specType;
}
