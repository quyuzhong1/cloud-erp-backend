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

    /**
     * 主键id 无id：新增 有id：修改
     */
    @ApiModelProperty(value = "主键id 无id：新增 有id：修改")
    private String id;

    /**
     * sku表id
     */
    @ApiModelProperty(value = "sku表id")
    private String skuId;

    /**
     * 证书图片
     */
    @ApiModelProperty(value = "证书图片")
    private String certificateImg;

    /**
     * 证书有效期
     */
    @ApiModelProperty(value = "证书有效期")
    private Date certificateValidTime;

    private static final long serialVersionUID = 1L;
}