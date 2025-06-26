package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_basic")
public class DictBasicEntity extends BaseEntity<DictBasicEntity> {

    /**
     * 当需要用到
     * 两个维度 获取值是可填
     */
    @TableField("remark")
    private String remark;

    /**
     * value 使用值
     */
    @TableField("value")
    private String value;

    /**
     * type 分组
     */
    @TableField("type")
    private String type;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 启用状态
     */
    @TableField("status")
    private Boolean status;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * type名称
     */
    @TableField("type_name")
    private String typeName;


    public static final String FIELD_VALUE = "value";

    public static final String FIELD_TYPE = "type";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_STATUS = "status";

    public static final String FIELD_SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
