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
 * 虚拟仓
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse")
public class VirtualWarehouseEntity extends BaseEntity<VirtualWarehouseEntity> {

    /**
    * 是否失效 true 失效 false 未失效
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;


    

    

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}