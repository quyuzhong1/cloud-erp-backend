package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 多规格sku信息请求参数
 * @Author: Luo_WG
 * @Date: 2022/9/21 15:46
 **/
@Data
@NoArgsConstructor
public class ProductDetailDTO implements Serializable {
    /**
     * 产品sku表id 无id：新增 有id：修改
     */
    @ApiModelProperty(value = "产品sku表id 无id：新增 有id：修改")
    private String id;

    /**
     * 产品表id
     */
    @ApiModelProperty(value = "产品表id")
    private String productId;

    /**
     * skuNo
     */
    @ApiModelProperty(value = "skuNo")
    private String skuNo;

    /**
     * 产品sku名称
     */
    @ApiModelProperty(value = "产品sku名称")
    private String name;

    /**
     * 变体属性
     */
    @ApiModelProperty(value = "变体属性")
    private String variantProperty;

    /**
     * 计划上市时间
     */
    @ApiModelProperty(value = "计划上市时间")
    private Date planListingTime;

    /**
     * 单位表id
     */
    @ApiModelProperty(value = "单位表id")
    private String unitId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发")
    private Integer productState;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    /**
     * 修改人id
     */
    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    /**
     * sku图片
     */
    @ApiModelProperty(value = "sku图片")
    private String imagesUrl;

    /**
     * 单位名称
     */
    @ApiModelProperty(value = "单位名称")
    private String unitName;

    /**
     * 产品负责人id
     */
    @ApiModelProperty(value = "产品负责人id")
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    @ApiModelProperty(value = "产品负责人姓名")
    private String chargeName;

    private static final long serialVersionUID = 1L;
}
