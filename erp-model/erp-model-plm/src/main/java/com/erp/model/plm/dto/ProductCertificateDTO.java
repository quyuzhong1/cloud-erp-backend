package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.*;
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
public class ProductCertificateDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "sku表id 无id：新增 有id：修改")
    private String skuId;

    @ApiModelProperty(value = "证书图片")
    private String certificateImg;

    @ApiModelProperty(value = "证书有效期")
    private Date certificateValidTime;

    @TableField(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    private static final long serialVersionUID = 1L;
}