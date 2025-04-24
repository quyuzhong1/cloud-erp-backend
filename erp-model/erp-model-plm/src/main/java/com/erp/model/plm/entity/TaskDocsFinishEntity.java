package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 任务文档交付表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_finish_docs")
public class TaskDocsFinishEntity extends BaseEntity<TaskDocsFinishEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件fstdfs 地址
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 创建时间
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * 文件后缀
     */
    @TableField(value = "file_suffix")
    private String fileSuffix;

    @TableField("file_size")
    private Double fileSize;

    @TableField("task_id")
    private String taskId;

    @TableField("task_docs_id")
    private String taskDocsId;

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;

    @TableField("upload_type")
    private Integer uploadType;

    @TableField("old_file_url")
    private String oldFileUrl;

    @TableField("old_upload_type")
    private Integer oldUploadType;

    @TableField("old_file_name")
    private String oldFileName;


}
