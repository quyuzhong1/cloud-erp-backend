package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 专题与布局关系表(BiSubjectRefLayout)实体类
 *
 * @author yl
 * @since 2022-12-08 12:31:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_subject_ref_layout")
public class BiSubjectRefLayoutEntity implements Serializable {
    private static final long serialVersionUID = 769162199098811663L;


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 主题id
     */
    private String subjectId;
    /**
     * 布局表id
     */
    private String layoutId;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 删除标示0未删除  1 被删除
     */

    @TableLogic
    private Integer isDeleted=0;



}

