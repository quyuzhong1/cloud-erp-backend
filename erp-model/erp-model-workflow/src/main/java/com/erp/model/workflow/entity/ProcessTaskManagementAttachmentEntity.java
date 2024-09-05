package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 审核附件表
 * </p>
 *
 * @author tmj
 * @since 2024-09-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("process_task_management_attachment")
public class ProcessTaskManagementAttachmentEntity extends BaseEntity<ProcessTaskManagementAttachmentEntity> {

    /**
    * 文件url
    */
    @TableField("attach_url")
    private String attachUrl;
    /**
    * 文件名称
    */
    @TableField("attach_name")
    private String attachName;

    /**
     * process_task_management表ID
     */
    @TableField("main_id")
    private String mainId;


    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}