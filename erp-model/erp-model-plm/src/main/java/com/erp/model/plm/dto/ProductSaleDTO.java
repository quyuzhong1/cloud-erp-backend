package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class ProductSaleDTO implements Serializable {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "sku表id 无id：新增 有id：修改")
    private String skuId;

    /**
     * 年目标销售量
     */
    @ApiModelProperty(value = "年目标销售量")
    private Integer yearSaleQty;

    /**
     * 年目标销售额
     */
    @ApiModelProperty(value = "年目标销售额")
    private BigDecimal yearSaleAmount;

    /**
     * 月目标销售量
     */
    @ApiModelProperty(value = "月目标销售量")
    private Integer monthSaleQty;

    /**
     * 月目标销售额
     */
    @ApiModelProperty(value = "月目标销售额")
    private BigDecimal monthSaleAmount;

    /**
     * 销售国家
     */
    @ApiModelProperty(value = "销售国家")
    private String saleCountry;

    /**
     * 上市时间
     */
    @ApiModelProperty(value = "上市时间")
    private Date listingTime;

    /**
     * 退市时间
     */
    @ApiModelProperty(value = "退市时间")
    private Date delistingTime;

    /**
     * 图片是否完成 1.是 2.否
     */
    @StateEnumValue(intValues = {1, 2}, message = "图片是否完成0或者1")
    @ApiModelProperty(value = "图片是否完成")
    private Integer isFinishedImg;

    /**
     * 视频是否完成 1.是 2.否
     */
    @ApiModelProperty(value = "视频是否完成 1.是 2.否")
    private Integer isFinishedVideo;

    /**
     * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
     */
    @ApiModelProperty(value = "销售状态 1.未销售 2.销售中 3.清仓中 4.已下架")
    private Integer saleState;

    /**
     * 产品上市（含培训）资料链接
     */
    @ApiModelProperty(value = "产品上市（含培训）资料链接")
    private String dataUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}