package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 产品证书表
 * @Author Luo_WG
 * @Date 2022/9/23 15:22
 **/
@Data
@NoArgsConstructor
public class ProductCertificateShowDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "产品sku表id")
    private String skuId;

    @ApiModelProperty(value = "产品sku图片")
    private String imagesUrl;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "证书图片")
    private String certificateImg;

    @ApiModelProperty(value = "证书有效期")
    private Date certificateValidTime;

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