package com.erp.model.plm.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Description 产品证书表
 * @Author Luo_WG
 * @Date 2022/9/23 15:22
 **/
@TableName(value ="product_certificate")
@Data
public class ProductCertificateEntity extends BaseEntity<ProductCertificateEntity> implements Serializable {

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 证书有效期
     */
    @TableField(value = "certificate_valid_time")
    private LocalDate certificateValidTime;

    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 证书类型
     */
    @TableField(value = "type")
    private String type;

    /**
     * 证书项目
     */
    @TableField(value = "dict_project")
    private String dictProject;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 附件
     */
    @TableField(exist = false)
    @JSONField(serialize = false)
    private MultipartFile multipartFile;

    /**
     * 附件路径
     */
    @TableField(exist = false)
    @JSONField(serialize = false)
    private String pathUrl;

    /**
     * 证书文件表id
     */
    @TableField(exist = false)
    private String attachmentId;

    public static final String TABLE_NAME = "product_certificate";

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}