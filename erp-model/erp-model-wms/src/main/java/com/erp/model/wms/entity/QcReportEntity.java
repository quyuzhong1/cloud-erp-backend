package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 质检报告
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_report")
public class QcReportEntity extends BaseEntity<QcReportEntity> {

    /**
     * 质检项 
     */
    @TableField("name")
    private String name;

    /**
     * 质检内容
     */
    @TableField("content")
    private String content;

    /**
     * 质检规则id
     */
    @TableField("qc_rule_id")
    private String qcRuleId;


    public static final String NAME = "name";

    public static final String CONTENT = "content";

    public static final String QCRULEID = "qcRuleId";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
