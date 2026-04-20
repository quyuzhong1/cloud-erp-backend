package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


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
public class TmsAttachmentEntity extends BaseEntity<TmsAttachmentEntity> {

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

    public static final String BUSINESS_ID = "business_id";

    public static final String TYPE = "type";

    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}