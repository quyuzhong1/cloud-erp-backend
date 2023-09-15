package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 主题分享表(BiSubjectShare)实体类
 *
 * @author yl
 * @since 2022-12-08 12:31:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_subject_share")
public class BiSubjectShareEntity implements Serializable {
    private static final long serialVersionUID = 639300965486063105L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 主题id
     */
    private String subjectId;

    /**
     * 身份ID
     * 用户ID/角色ID
     */
    @TableField(value = "identity_id")
    private String identityId;

    /**
     * 引用类型: 用户=user, 角色=role
     * {@link BiShareIdentityTypeEnum}
     */
    @TableField(value = "identity_type")
    private String identityType;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更改人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 删除标示0未删除  1 被删除
     */

    @TableLogic
    private Boolean isDeleted=false;



}

