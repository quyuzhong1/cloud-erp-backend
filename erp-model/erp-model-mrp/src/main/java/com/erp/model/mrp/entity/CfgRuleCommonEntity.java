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
     * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
     */
    @TableField("platform_type")
    private String platformType;

    /**
     * 是否默认
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 字段类型，single单选,multiple多选
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 分类名称
     */
    @TableField("category_name")
    private String categoryName;

    public static final String INDEX = "index";

    public static final String PARENT_ID = "parent_id";

    public static final String NAME = "name";

    public static final String VALUE = "value";

    public static final String DESCRIPTION = "description";

    public static final String DISABLE = "disable";

    public static final String TYPE = "type";

    public static final String PLATFORM_TYPE = "platform_type";

    public static final String IS_DEFAULT = "is_default";

    public static final String FIELD_TYPE = "field_type";

    public static final String CATEGORY_NAME = "category_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}