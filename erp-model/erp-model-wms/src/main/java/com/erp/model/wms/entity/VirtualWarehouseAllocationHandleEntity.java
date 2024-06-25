package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 分货单拆单主表
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_allocation_handle")
@AllArgsConstructor
@NoArgsConstructor
public class VirtualWarehouseAllocationHandleEntity extends BaseEntity<VirtualWarehouseAllocationHandleEntity> {

    /**
    * 分货单id
    */
    @TableField("allocation_id")
    private String allocationId;
    /**
    * 编号
    */
    @TableField("allocation_code")
    private String allocationCode;
    /**
    * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
    */
    @TableField("type")
    private String type;
    /**
    * 状态 ：waitSubmit待提交 handle已处理 invalid已作废
    */
    @TableField("status")
    private String status;
    /**
    * 方向
    */
    @TableField("direction")
    private Integer direction;


    public static final String ALLOCATION_ID = "allocation_id";

    public static final String ALLOCATION_CODE = "allocation_code";

    public static final String TYPE = "ype";

    public static final String STATUS = "status";

    public static final String DIRECTION = "direction";

    @Override
    public Serializable pkVal() {
        return null;
    }

}