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
    @ApiModelProperty(value = "产品sku表id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "产品表id")
    private String productId;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "产品sku名称")
    private String name;

    @ApiModelProperty(value = "属性")
    private String attribute;

    @ApiModelProperty(value = "计划上市时间")
    private Date planListingTime;

    @ApiModelProperty(value = "单位表id")
    private String unitId;

    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发")
    private Integer productState;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    @ApiModelProperty(value = "sku图片")
    private String imagesUrl;

    private static final long serialVersionUID = 1L;
}
