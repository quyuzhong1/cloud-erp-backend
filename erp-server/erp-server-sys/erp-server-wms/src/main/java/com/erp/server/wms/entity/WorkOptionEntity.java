package com.erp.server.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("work_option")
public class WorkOptionEntity extends BaseEntity<WorkOptionEntity> {

    /**
     * 字典表id
     */
    @TableField("dict_basic_id")
    private String dictBasicId;

    /**
     * 字典名称
     */
    @TableField("dict_basic_name")
    private String dictBasicName;

    /**
     * 类型  1：常用模块  2：代办模块
     */
    @TableField("type")
    private String type;

    /**
     * 模块名称
     */
    @TableField("name")
    private String name;


    public static final String DICT_BASIC_ID = "dict_basic_id";

    public static final String DICT_BASIC_NAME = "dict_basic_name";

    public static final String TYPE = "type";

    public static final String NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
