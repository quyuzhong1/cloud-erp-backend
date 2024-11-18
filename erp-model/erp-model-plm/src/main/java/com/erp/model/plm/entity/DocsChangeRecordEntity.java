package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.common.core.entity.BaseEntity;
import lombok.Data;

/**
 * 文档变更记录
 * @TableName docs_change_record
 */
@TableName(value ="docs_change_log")
@Data
public class DocsChangeRecordEntity extends BaseEntity<DocsChangeRecordEntity> implements Serializable {
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}