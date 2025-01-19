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
 * 汉化管理规则表
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("rule_prompt_word")
public class RulePromptWordEntity extends BaseEntity<RulePromptWordEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 禁用状态false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 规则描述
    */
    @TableField("desc")
    private String desc;
    /**
    * 提示
    */
    @TableField("tips")
    private String tips;
    /**
    * 解决方案
    */
    @TableField("solution")
    private String solution;
    /**
    * 优先级
    */
    @TableField("index")
    private Integer index;


    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String DESC = "desc";

    public static final String TIPS = "tips";

    public static final String SOLUTION = "solution";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}