package com.erp.model.tms.entity;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 对账字段配置表
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_reconciliation_field")
public class CfgReconciliationFieldEntity extends BaseEntity<CfgReconciliationFieldEntity> {

    /**
     * 核对类型
     */
    @TableField("reconciliation_type")
    private String reconciliationType;
    /**
     * 第三方名称
     */
    @TableField("third_name")
    private String thirdName;
    /**
     * 第三方名称代号
     */
    @TableField("third_code")
    private String thirdCode;
    /**
     * 第三方字段名称
     */
    @TableField("third_field_name")
    private String thirdFieldName;
    /**
     * ERP字段来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * ERP字段来源ID
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 启用状态
     */
    @TableField("status")
    private Boolean status;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String RECONCILIATION_TYPE = "reconciliation_type";

    public static final String THIRD_NAME = "third_name";

    public static final String THIRD_CODE = "third_code";

    public static final String THIRD_FIELD_NAME = "third_field_name";

    public static final String ERP_FIELD_NAME = "erp_field_name";

    public static final String ERP_FIELD = "erp_field";

    public static final String FIELD_STATUS = "status";

    public static final String FIELD_REMARK = "remark";

    /**
     * 唯一键
     */
    public static String combineUniqueCode(String reconciliationType, String supplierId, String sourceType, String sourceId) {
        return CharSequenceUtil.format("{}_{}_{}_{}", reconciliationType, supplierId, sourceType, sourceId);
    }

    /**
     * 当前唯一键
     */
    public String currentUniqueCode() {
        return CharSequenceUtil.format("{}_{}_{}_{}", this.reconciliationType, this.thirdCode, this.sourceType, this.sourceId);
    }


}