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
 * 拣货车类型
 * </p>
 *
 * @author will
 * @since 2024-06-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("picking_cart_type")
public class PickingCartTypeEntity extends BaseEntity<PickingCartTypeEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;

    /**
     * 排序字段
     */
    private Integer index;

    @Override
    public Serializable pkVal() {
        return null;
    }

}