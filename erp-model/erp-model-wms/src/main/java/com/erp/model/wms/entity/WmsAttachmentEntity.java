package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

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
public class WmsAttachmentEntity extends BaseEntity<WmsAttachmentEntity> {

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
    /**
     * 文件版本号
     */
    @TableField("attach_version")
    private Integer attachVersion;

    /**
     * 文件大小
     */
    @TableField("attach_size")
    private BigDecimal attachSize;


    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    public static final String BUSINESS_ID = "business_id";

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}
