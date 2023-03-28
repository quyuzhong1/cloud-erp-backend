package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 项目模板信息
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_template")
public class ProjectTemplateEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板名
     */
    @TableField("name")
    private String name;

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 模板状态(1启用，0禁用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否默认，项目模板存在默认数据(1默认，0非默认)
     */
//    @TableField("is_default")
//    private Integer isDefault;


    /**
     * 产品属性id
     * 对应basic_dict 表 type=productProperty 表id
     */
//    @TableField("product_property_id")
//    private String productPropertyId;

}
