package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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


    public static final String DISABLED = "disabled";

    public static final String CODE = "code";

    public static final String NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}