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
@TableName("qc_defect")
public class QcDefectEntity extends BaseEntity<QcDefectEntity> {

    /**
    * 质检单id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 缺陷等级
    */
    @TableField("defect_level")
    private String defectLevel;
    /**
    * 缺陷数量
    */
    @TableField("bad_qty")
    private Integer badQty;
    /**
    * 不良描述
    */
    @TableField("defect_desc")
    private String defectDesc;
    /**
    * 问题属性
    */
    @TableField("issue_property")
    private String issueProperty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String DEFECT_LEVEL = "defect_level";

    public static final String DEFECT_QTY = "defect_qty";

    public static final String DEFECT_DESC = "defect_desc";

    public static final String ISSUE_PROPERTY = "issue_property";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}