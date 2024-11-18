package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 拣货车管理
 * </p>
 *
 * @author will
 * @since 2024-06-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("picking_cart")
public class PickingCartEntity extends BaseEntity<PickingCartEntity> {

    /**
    * 编号
    */
    @TableField("code")
    private String code;
    /**
    * 拣货车类型id
    */
    @TableField("type_id")
    private String typeId;
    /**
    * 状态,true是，false否
    */
    @TableField("disabled")
    private Boolean disabled;

    public static final String TYPE_ID = "type_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}