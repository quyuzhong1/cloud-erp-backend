package com.erp.model.srm.entity;

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
 * 公共附件表
 * </p>
 *
 * @author will
 * @since 2024-01-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
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
    * 附件的文件地址
    */
    @TableField("attach_url")
    private String attachUrl;
    /**
    * 附件的文档的名称
    */
    @TableField("attach_name")
    private String attachName;


    public static final String BUSINESS_ID = "business_id";

    public static final String TYPE = "type";

    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}