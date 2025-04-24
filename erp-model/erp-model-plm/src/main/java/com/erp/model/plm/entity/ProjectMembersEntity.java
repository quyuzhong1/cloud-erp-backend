package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 项目成员表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_members")
public class ProjectMembersEntity extends BaseEntity<ProjectMembersEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员id
     */
    @TableField("member_id")
    private String memberId;

    /**
     * 成员名
     */
    @TableField("member_name")
    private String memberName;

    /**
     * 项目id
     */
    @TableField("project_id")
    private String projectId;

    /**
     * 是否是负责人 0 不是 1 是
     */
    @TableField("is_charge")
    private Integer isCharge;

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;

}
