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
 * 
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_standard_ref")
public class QcStandardRefEntity extends BaseEntity<QcStandardRefEntity> {

    /**
    * 质检单id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 质检项目
    */
    @TableField("inspect_item")
    private String inspectItem;
    /**
    * 质检要求
    */
    @TableField("inspect_requirement")
    private String inspectRequirement;


    public static final String MAIN_ID = "main_id";

    public static final String SAMPLING_PLAN_ID = "sampling_plan_id";

    public static final String SUGGEST_SAMPLING_QTY = "suggest_sampling_qty";

    public static final String INSPECT_ITEM = "inspect_item";

    public static final String INSPECT_REQUIREMENT = "inspect_requirement";

    public static final String ATTACH_URL = "attach_url";

    public static final String ATTACH_NAME = "attach_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}