package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 分货单拆单明细表
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_push_handle_detail")
public class VirtualWarehousePushHandleDetailEntity extends BaseEntity<VirtualWarehousePushHandleDetailEntity> {

    /**
    * 同步状态-0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败
    */
    @TableField("status")
    private String status;
    /**
    * 分货单id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 调出虚拟仓id
    */
    @TableField("from_virtual_warehouse_id")
    private String fromVirtualWarehouseId;
    /**
    * 调入虚拟仓id
    */
    @TableField("to_virtual_warehouse_id")
    private String toVirtualWarehouseId;
    /**
    * 第三方调出虚拟仓id
    */
    @TableField("third_from_virtual_warehouse_id")
    private String thirdFromVirtualWarehouseId;
    /**
    * 第三方调出虚拟仓id
    */
    @TableField("third_from_virtual_warehouse_no")
    private String thirdFromVirtualWarehouseNo;
    /**
    * 第三方实体仓id
    */
    @TableField("third_warehouse_id")
    private String thirdWarehouseId;
    /**
    * 第三方调入虚拟仓id
    */
    @TableField("third_to_virtual_warehouse_id")
    private String thirdToVirtualWarehouseId;
    /**
    * 第三方调入虚拟仓id
    */
    @TableField("third_to_virtual_warehouse_no")
    private String thirdToVirtualWarehouseNo;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
    */
    @TableField("type")
    private String type;
    /**
    * 方向
    */
    @TableField("direction")
    private Integer direction;
    /**
    * 第三方单号
    */
    @TableField("code")
    private String code;
    /**
    * 系统类型：wdt旺店通
    */
    @TableField("sys_type")
    private String sysType;
    /**
    * 分货单拆单主表id
    */
    @TableField("main_id")
    private String mainId;


    

    public static final String ALLOCATION_ID = "allocation_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String FROM_VIRTUAL_WAREHOUSE_ID = "from_virtual_warehouse_id";

    public static final String TO_VIRTUAL_WAREHOUSE_ID = "to_virtual_warehouse_id";

    public static final String THIRD_FROM_VIRTUAL_WAREHOUSE_ID = "third_from_virtual_warehouse_id";

    public static final String THIRD_TO_VIRTUAL_WAREHOUSE_ID = "third_to_virtual_warehouse_id";

    

    

    

    

    public static final String SYS_TYPE = "sys_type";

    public static final String HANDLE_ID = "handle_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}