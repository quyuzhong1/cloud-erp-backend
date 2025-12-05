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
 * 差异策略配置条件
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_diff_strategy_condition")
public class CfgDiffStrategyConditionEntity extends BaseEntity<CfgDiffStrategyConditionEntity> {

    /**
    * 明细id
    */
    @TableField("detail_id")
    private String detailId;
    /**
    * 左括号
    */
    @TableField("left_bracket")
    private String leftBracket;
    /**
    * 条件的字段
    */
    @TableField("field")
    private String field;
    /**
    * 比较符
    */
    @TableField("compare")
    private String compare;
    /**
    * 对应的值
    */
    @TableField("value")
    private String value;
    /**
    * 右括号
    */
    @TableField("right_bracket")
    private String rightBracket;
    /**
    * 逻辑关系 or 和 and
    */
    @TableField("logic")
    private String logic;
    /**
    * 顺序
    */
    @TableField("index")
    private Integer index;


    public static final String DETAIL_ID = "detail_id";

    public static final String LEFT_BRACKET = "left_bracket";

    public static final String FIELD = "field";

    public static final String COMPARE = "compare";

    public static final String VALUE = "value";

    public static final String RIGHT_BRACKET = "right_bracket";

    public static final String LOGIC = "logic";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}