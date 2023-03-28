package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 公共附件表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("attachment")
public class AttachmentEntity extends BaseEntity<AttachmentEntity> {

    /**
     * 业务表id
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 类型 存表名
     */
    @TableField("type")
    private String type;

    /**
     * 附件url
     */
    @TableField("attach_url")
    private String attachUrl;

    /**
     * 附件名称
     */
    @TableField("attach_name")
    private String attachName;


    static final String BUSINESS_ID = "business_id";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
