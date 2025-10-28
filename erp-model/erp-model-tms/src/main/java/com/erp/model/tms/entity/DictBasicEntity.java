package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 字典表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_basic")
public class DictBasicEntity extends BaseEntity<DictBasicEntity> {

    /**
    * 标识code 值
    */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 类型
    */
    @TableField("type")
    private String type;
    /**
    * 类型名称
    */
    @TableField("type_name")
    private String typeName;


    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    /**
     * 排序字段
     */
    @TableField("index")
    private Integer index;
    
    /**
     * 启用状态
     */
    @TableField("status")
    private Boolean status;

    public static final String FIELD_CODE = "code";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_TYPE = "type";

    public static final String TYPE_NAME = "type_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}