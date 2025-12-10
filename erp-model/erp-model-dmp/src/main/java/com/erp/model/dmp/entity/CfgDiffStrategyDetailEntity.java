package com.erp.model.dmp.entity;

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
 * 差异策略配置明细
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_diff_strategy_detail")
public class CfgDiffStrategyDetailEntity extends BaseEntity<CfgDiffStrategyDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 差异标签
    */
    @TableField("diff_tag")
    private String diffTag;
    /**
    * 建议处理方式
    */
    @TableField("suggest_type")
    private String suggestType;
    /**
    * 条件sql
    */
    @TableField("condition_sql")
    private String conditionSql;
    /**
    * 条件描述
    */
    @TableField("condition_desc")
    private String conditionDesc;


    public static final String MAIN_ID = "main_id";

    public static final String DIFF_TAG = "diff_tag";

    public static final String SUGGEST_TYPE = "suggest_type";

    public static final String CONDITION_SQL = "condition_sql";

    public static final String CONDITION_DESC = "condition_desc";

    @Override
    public Serializable pkVal() {
        return null;
    }

}