package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.common.annotation.StateEnumValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品销售信息表请求参数
 * @Author Luo_WG
 * @Date 2022/9/23 12:14
 **/
@Data
@NoArgsConstructor
public class ProductSaleShowDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "产品sku表id")
    private String skuId;

    @ApiModelProperty(value = "产品sku图片")
    private String imagesUrl;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "年目标销售量")
    private Integer yearSaleQty;

    @ApiModelProperty(value = "年目标销售额")
    private BigDecimal yearSaleAmount;

    @ApiModelProperty(value = "月目标销售量")
    private Integer monthSaleQty;

    @ApiModelProperty(value = "月目标销售额")
    private BigDecimal monthSaleAmount;

    @ApiModelProperty(value = "销售国家")
    private String saleCountry;

    @ApiModelProperty(value = "上市时间")
    private Date listingTime;

    @ApiModelProperty(value = "退市时间")
    private Date delistingTime;

    @StateEnumValue(intValues = {1, 2}, message = "图片是否完成0或者1")
    @ApiModelProperty(value = "图片是否完成")
    private Integer isFinishedImg;

    @ApiModelProperty(value = "视频是否完成 1.是 2.否")
    private Integer isFinishedVideo;

    @ApiModelProperty(value = "销售状态 1.未销售 2.销售中 3.清仓中 4.已下架")
    private Integer saleState;

    @ApiModelProperty(value = "产品上市（含培训）资料链接")
    private String dataUrl;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;

    private static final long serialVersionUID = 1L;
}