package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

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
public class ProjectTemplateEntity extends BaseEntity<ProjectTemplateEntity> implements Serializable {

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

}
