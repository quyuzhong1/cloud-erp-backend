package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

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
public class TaskDocsFinishEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务文档关联表id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

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

    /**
     * 创建人
     */
    @TableField("create_user_name")
    private String createUserName;

    @TableField("file_size")
    private Double fileSize;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    @TableField("task_id")
    private String taskId;

    @TableField("task_docs_id")
    private String taskDocsId;



}
