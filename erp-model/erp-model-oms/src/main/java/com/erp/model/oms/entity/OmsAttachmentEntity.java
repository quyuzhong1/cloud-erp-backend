package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author lambda
 * @since 2023-04-19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("attachment")
public class OmsAttachmentEntity extends BaseEntity<OmsAttachmentEntity> {

    /**
     * 文件地址url
     */
    @TableField("attach_url")
    private String attachUrl;

    /**
     * 文件名
     */
    @TableField("attach_name")
    private String attachName;

    /**
     * 业务表id
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 类型
     */
    @TableField("type")
    private String type;


    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    public static final String BUSINESS_ID = "business_id";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
