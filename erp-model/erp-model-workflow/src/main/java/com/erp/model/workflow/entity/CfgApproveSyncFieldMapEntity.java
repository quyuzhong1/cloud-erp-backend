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
 * ERP审批同步-推送信息配置
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_approve_sync_field_map")
public class CfgApproveSyncFieldMapEntity extends BaseEntity<CfgApproveSyncFieldMapEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 国际化类型 默认zh-CN
    */
    @TableField("locale")
    private String locale;
    /**
     * 字段id
     */
    @TableField("field_id")
    private String fieldId;
    /**
    * 字段名
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 字段来源
    */
    @TableField("field_source")
    private String fieldSource;
    /**
    * 是否快捷审批
    */
    @TableField("is_quick")
    private Boolean isQuick;
    /**
    * 排序
    */
    @TableField("sort")
    private Integer sort;


    public static final String MAIN_ID = "main_id";

    public static final String LOCALE = "locale";

    public static final String FIELD_ID = "field_id";

    public static final String FIELD_NAME = "field_name";

    public static final String FIELD_SOURCE = "field_source";

    public static final String IS_QUICK = "is_quick";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}