package com.erp.model.wms.entity;

import java.math.BigDecimal;
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
 * 抽样方案表
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_sampling_plan")
public class QcSamplingPlanEntity extends BaseEntity<QcSamplingPlanEntity> {

    /**
    * 方案编码（CYFA）
    */
    @TableField("code")
    private String code;
    /**
    * 方案类型
     * PlanTypeEnum
    */
    @TableField("plan_type")
    private String planType;
    /**
    * 检验水平
     * QcLevelEnum
    */
    @TableField("qc_level")
    private String qcLevel;
    /**
    * 严重缺陷AQL
     * AqlValueEnum
    */
    @TableField("major_aql")
    private String majorAql;
    /**
    * 一般缺陷AQL
     * AqlValueEnum
    */
    @TableField("general_aql")
    private String generalAql;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
     * skuId 后端识别数据使用
     */
    @TableField(exist = false)
    private String skuId;


    public static final String CODE = "code";

    public static final String plan_type = "plan_type";

    public static final String QC_LEVEL = "qc_level";

    public static final String MAJOR_AQL = "major_aql";

    public static final String GENERAL_AQL = "general_aql";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}