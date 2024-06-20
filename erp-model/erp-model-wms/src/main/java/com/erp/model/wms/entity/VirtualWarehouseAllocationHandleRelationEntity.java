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
 * 分货单拆单关联关系表
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_allocation_handle_relation")
public class VirtualWarehouseAllocationHandleRelationEntity extends BaseEntity<VirtualWarehouseAllocationHandleRelationEntity> {

    /**
    * 分货单id
    */
    @TableField("allocation_id")
    private String allocationId;
    /**
    * 分货单明细id
    */
    @TableField("allocation_detail_id")
    private String allocationDetailId;
    /**
    * 分货单拆单表id
    */
    @TableField("handle_id")
    private String handleId;
    /**
    * 分货单拆单明细表id
    */
    @TableField("handle_detail_id")
    private String handleDetailId;


    public static final String ALLOCATION_ID = "allocation_id";

    public static final String ALLOCATION_DETAIL_ID = "allocation_detail_id";

    public static final String HANDLE_ID = "handle_id";

    public static final String HANDLE_DETAIL_ID = "handle_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}