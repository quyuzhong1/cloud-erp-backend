package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 布局模块取消记录表(BiLayoutModuleCancelLog)实体类
 *
 * @author yl
 * @since 2022-12-14 15:16:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_layout_module_cancel_log")
public class BiLayoutModuleCancelLogEntity implements Serializable {
    private static final long serialVersionUID = 600876241880664691L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
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
     * 专题id
     */
    private String subjectId;
    /**
     * 布局id
     */
    private String layoutId;
    /**
     * 取消的模块d
     */
    private String cancelModuleId;



}

