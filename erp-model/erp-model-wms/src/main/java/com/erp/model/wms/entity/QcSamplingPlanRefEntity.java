package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2026-03-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_sampling_plan_ref")
public class QcSamplingPlanRefEntity extends BaseEntity<QcSamplingPlanRefEntity> {

    /**
    * 质检单id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 抽样方案id
    */
    @TableField("sampling_plan_id")
    private String samplingPlanId;
    /**
     * 抽样方案编码
     */
    @TableField("sampling_plan_code")
    private String samplingPlanCode;
    /**
    * 建议抽样数量
    */
    @TableField("suggest_sampling_qty")
    private Integer suggestSamplingQty;
    /**
    * 附件url
    */
    @TableField("attach_url")
    private String attachUrl;
    /**
    * 附件名称
    */
    @TableField("attach_name")
    private String attachName;


    public static final String MAIN_ID = "main_id";

    public static final String SAMPLING_PLAN_ID = "sampling_plan_id";

    public static final String SUGGEST_SAMPLING_QTY = "suggest_sampling_qty";

    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}