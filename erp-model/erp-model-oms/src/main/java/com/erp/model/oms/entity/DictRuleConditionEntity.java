package com.erp.model.oms.entity;

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
 * 条件字典表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_rule_condition")
public class DictRuleConditionEntity extends BaseEntity<DictRuleConditionEntity> {

    /**
    * 对应唯一
    */
    @TableField("key")
    private String key;
    /**
    * 名称
    */
    @TableField("value")
    private String value;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 类型
    */
    @TableField("type")
    private String type;


    public static final String KEY = "key";

    public static final String VALUE = "value";

    public static final String REMARK = "remark";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}