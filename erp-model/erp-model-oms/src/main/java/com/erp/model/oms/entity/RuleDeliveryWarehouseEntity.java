package com.erp.model.oms.entity;

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
 * 发货仓库规则表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("rule_delivery_warehouse")
public class RuleDeliveryWarehouseEntity extends BaseEntity<RuleDeliveryWarehouseEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 禁用状态false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注描述
    */
    @TableField("remark")
    private String remark;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名
    */
    @TableField("warehouse_name")
    private String warehouseName;


    public static final String NAME = "name";

    public static final String PRIORITY = "priority";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}