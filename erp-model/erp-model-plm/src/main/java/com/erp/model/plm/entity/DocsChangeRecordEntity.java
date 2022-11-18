package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文档变更记录
 * @TableName docs_change_record
 */
@TableName(value ="docs_change_log")
@Data
public class DocsChangeRecordEntity implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 需要完成的文档id
     */
    private String finishDocsId;

    /**
     * 内容
     */
    private String content;

    /**
     * 变更流程id
     */
    private String processId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名
     */
    private String createUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}