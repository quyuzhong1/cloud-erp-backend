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
@TableName("scm_document")
public class ScmDocumentEntity extends BaseEntity<ScmDocumentEntity> {

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


    public static final String BUSINESS_ID = "business_id";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
