package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 金蝶字典表
 * </p>
 *
 * @author lrp
 * @since 2024-06-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_kingdee")
public class DictKingdeeEntity extends BaseEntity<DictKingdeeEntity> {

    /**
    * 金蝶id
    */
    @TableField("kingdee_id")
    private String kingdeeId;
    /**
    * 类型名称
    */
    @TableField("type_name")
    private String typeName;
    /**
    * 字典值
    */
    @TableField("value")
    private String value;
    /**
    * 字典名
    */
    @TableField("name")
    private String name;
    /**
    * 是否已作废
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String KINGDEE_ID = "kingdee_id";

    public static final String TYPE_NAME = "type_name";

    public static final String FIELD_VALUE = "value";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}