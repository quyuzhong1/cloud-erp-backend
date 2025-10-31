package com.erp.model.dmp.entity;

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
 * @author will
 * @since 2023-05-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_basic")
public class DictBasicEntity extends BaseEntity<DictBasicEntity> {

    /**
     * 备注 需要的时候 用到
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
     * 类型名称
     */
     @TableField("type_name")
     private String typeName;


    public static final String REMARK = "remark";

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
