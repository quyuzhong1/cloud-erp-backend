package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 布局与模块关系表(BiLayoutRefModule)实体类
 *
 * @author yl
 * @since 2022-12-08 12:31:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_layout_ref_module")
public class BiLayoutRefModuleEntity implements Serializable {
    private static final long serialVersionUID = 613744391707551580L;

    /**
     * 表id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 模块编号(例如四分之一块)
     */
    private String blockNo;
    /**
     * 布局id
     */
    private String layoutId;
    /**
     * 模块表id
     */
    private String moduleId;

    /**
     * 专题id
     */
    private String subjectId;
    /**
     * 序号
     */
    private Integer serialNo;


    /**
     * 创建人
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 修改人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



}

