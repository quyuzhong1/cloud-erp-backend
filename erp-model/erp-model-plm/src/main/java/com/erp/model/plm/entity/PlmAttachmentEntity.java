package com.erp.model.plm.entity;

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
 * 附件表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("attachment")
public class PlmAttachmentEntity extends BaseEntity<PlmAttachmentEntity> {


    /**
    * 业务表id
    */
    @TableField("business_id")
    private String businessId;

    /**
    * 类型 默认表名
    */
    @TableField("type")
    private String type;

    /**
    * 附件地址
    */
    @TableField("attach_url")
    private String attachUrl;

    /**
    * 附件名称
    */
    @TableField("attach_name")
    private String attachName;

    /**
     * 附件大小
     */
    @TableField("attach_size")
    private BigDecimal attachSize;


    public static final String BUSINESS_ID = "business_id";

    public static final String FIELD_TYPE = "type";

    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}