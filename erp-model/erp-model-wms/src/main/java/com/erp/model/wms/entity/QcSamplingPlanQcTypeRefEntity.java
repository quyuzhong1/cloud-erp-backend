package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 抽样方案质检类型关联表
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_sampling_plan_qc_type_ref")
public class QcSamplingPlanQcTypeRefEntity extends BaseEntity<QcSamplingPlanQcTypeRefEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 质检类型
     * QcTypeEnum
    */
    @TableField("qc_type")
    private String qcType;
    /**
    * 状态(禁用true启用false)
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String MAIN_ID = "main_id";

    public static final String QC_TYPE = "qc_type";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}