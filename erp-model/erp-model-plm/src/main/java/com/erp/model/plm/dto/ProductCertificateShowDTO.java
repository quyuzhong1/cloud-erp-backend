package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description 产品证书表
 * @Author Luo_WG
 * @Date 2022/9/23 15:22
 **/
@Data
@NoArgsConstructor
public class ProductCertificateShowDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品sku表id
     */
    private String skuId;

    /**
     * 产品sku图片
     */
    private String imagesUrl;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 证书类型
     */
    private String type;

    /**
     * 证书类型名称
     */
    private String typeName;

    /**
     * 证书项目
     */
    private String dictProject;

    /**
     * 证书项目名称
     */
    private String dictProjectName;

    /**
     * 文件id
     * *
     */
    private String attachId;

    /**
     * 文件名称
     */
    private String attachName;

    /**
     * 文件URL
     */
    private String attachUrl;


    /**
     * 证书有效期
     */
    private LocalDate certificateValidTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     *禁止修改的字段
     */
    private List<String> disableFieldList;

    private static final long serialVersionUID = 1L;
}