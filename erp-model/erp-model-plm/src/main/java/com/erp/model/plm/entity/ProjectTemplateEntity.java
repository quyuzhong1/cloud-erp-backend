package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
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
public class ProjectTemplateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
     * 模板类型(1立项模板,2项目模板)
     */
    @TableField("type")
    private Integer type;

    /**
     * 模板状态(1启用，0禁用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否默认，项目模板存在默认数据(1默认，0非默认)
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 创建人
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新人
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 更新人id
     */
    @TableField("update_user_id")
    private String updateUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
