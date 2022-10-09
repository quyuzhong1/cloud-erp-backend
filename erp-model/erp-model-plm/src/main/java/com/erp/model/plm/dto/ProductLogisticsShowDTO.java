package com.erp.model.plm.dto;

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
public class ProductLogisticsShowDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    /**
     * 产品sku表id
     */
    @ApiModelProperty(value = "产品sku表id")
    private String skuId;

    /**
     * 产品sku图片
     */
    @ApiModelProperty(value = "产品sku图片")
    private String imagesUrl;

    /**
     * skuNo
     */
    @ApiModelProperty(value = "skuNo")
    private String skuNo;

    /**
     * 产品属性
     */
    @ApiModelProperty(value = "产品属性")
    private String productProperty;

    /**
     * 产品属性id
     */
    @ApiModelProperty(value = "产品属性id")
    private String productPropertyId;

    /**
     * 报关中文名
     */
    @ApiModelProperty(value = "报关中文名")
    private String declareChineseName;

    /**
     * 报关英文名
     */
    @ApiModelProperty(value = "报关英文名")
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    @ApiModelProperty(value = "报关申报价格")
    private BigDecimal declarePrice;

    /**
     * 海关编码
     */
    @ApiModelProperty(value = "海关编码")
    private String customsCode;

    /**
     * 申报要素
     */
    @ApiModelProperty(value = "申报要素")
    private String declareElement;

    /**
     * 英文材质
     */
    @ApiModelProperty(value = "英文材质")
    private String englishMaterial;

    /**
     * 英文用途
     */
    @ApiModelProperty(value = "英文用途")
    private String englishUsage;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    /**
     * 修改人id
     */
    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    /**
     * 修改人名称
     */
    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;

    private static final long serialVersionUID = 1L;
}