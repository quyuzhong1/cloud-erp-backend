package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 公共配置（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_common")
public class CfgRuleCommonEntity extends BaseEntity<CfgRuleCommonEntity> {

    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;
    /**
    * 上级id
    */
    @TableField("parent_id")
    private String parentId;
    /**
     * 编码
     */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 值
    */
    @TableField("value")
    private String value;
    /**
    * 值描述
    */
    @TableField("remark")
    private String remark;
    /**
    * 是否禁用
    */
    @TableField("disable")
    private Boolean disable;
    /**
    * 类型，（inventory库存配置，suggest建议配置）
    */
    @TableField("type")
    private String type;

    /**
     * 是否默认
     */
    @TableField("is_default")
    private Boolean isDefault;


    public static final String INDEX = "index";

    public static final String PARENT_ID = "parent_id";

    public static final String NAME = "name";

    public static final String VALUE = "value";

    public static final String DESCRIPTION = "description";

    public static final String DISABLE = "disable";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}