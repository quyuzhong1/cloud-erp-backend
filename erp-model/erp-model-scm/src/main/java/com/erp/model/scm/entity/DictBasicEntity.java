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
     * key 查询依据
     */
    @TableField("key")
    private String key;

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


    public static final String KEY = "key";

    public static final String VALUE = "value";

    public static final String TYPE = "type";

    public static final String NAME = "name";

    public static final String STATUS = "status";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
