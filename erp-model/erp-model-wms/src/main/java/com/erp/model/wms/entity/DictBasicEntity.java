package com.erp.model.wms.entity;

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
     * 备注 用到需要 用到的地方
     */
    @TableField("remark")
    private String remark;

    /**
     * value 使用值
     */
    @TableField("value")
    private String value;

    /**
     * type
     */
    @TableField("type")
    private String type;


    /**
     * typeName
     */
    @TableField("type_name")
    private String typeName;

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

    @Override
    public Serializable pkVal() {
        return null;
    }

}
