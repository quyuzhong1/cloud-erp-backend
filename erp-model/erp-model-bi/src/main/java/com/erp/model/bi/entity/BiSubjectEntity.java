package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专题表(BiSubject)实体类
 *
 * @author yl
 * @since 2022-12-08 12:31:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_subject")
public class BiSubjectEntity implements Serializable {
    private static final long serialVersionUID = -99930452849287333L;


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 专题名
     */
    private String name;
    /**
     * 是否是常用0 不是  1 是
     */
    private Integer isFrequently;
    /**
     * 分类名
     */
    private String categoryName;
    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分享标示
     * personal 私人
     * share 共享
     */
    private String shareFlag;


    /**
     * 启用状态
     * 1 启用
     * 0 未启用
     */
    private Integer state;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建人
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;
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
     * 更改人
     */
    @TableField(value = "update_user_name",fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标示0未删除  1 被删除
     */
    @TableLogic
    private Integer isDeleted=0;



}

