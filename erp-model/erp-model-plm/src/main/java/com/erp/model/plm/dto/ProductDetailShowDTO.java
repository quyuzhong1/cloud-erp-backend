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
    /**
     * 产品信息表id
     */
    @ApiModelProperty(value = "产品信息表id")
    private String id;

    /**
     * 产品sku信息表id
     */
    @ApiModelProperty(value = "产品sku信息表id")
    private String skuId;

    /**
     * imagesUrl
     */
    @ApiModelProperty(value = "图片")
    private String imagesUrl;

    /**
     * spuNo
     */
    @ApiModelProperty(value = "spuNo")
    private String spuNo;

    /**
     * skuNo
     */
    @ApiModelProperty(value = "skuNo")
    private String skuNo;

    /**
     * 产品名称
     */
    @ApiModelProperty(value = "产品名称")
    private String name;

    /**
     * 首批到货量
     */
    @ApiModelProperty(value = "首批到货量")
    private String actualArrivalNum;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    @ApiModelProperty(value = "首批到货状态：1.未到货 2.已到货 3.部分到货")
    private Date arrivalState;

    /**
     * 侵权风险
     */
    @ApiModelProperty(value = "侵权风险")
    private String pirateRisk;

    /**
     * 标准零售价
     */
    @ApiModelProperty(value = "标准零售价")
    private Integer retailPrice;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发")
    private String productState;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createUserId;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    @ApiModelProperty(value = "规格类型  1：无规格  2：多规格")
    private String specType;
}
