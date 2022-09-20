package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
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
@TableName("task_docs_finish")
public class TaskDocsFinishEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务文档关联表id
     */
    @TableId(value = "task_ref_docs_id", type = IdType.ASSIGN_ID)
    private String taskRefDocsId;

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
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @TableField("create_user")
    private String createUser;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;


}
