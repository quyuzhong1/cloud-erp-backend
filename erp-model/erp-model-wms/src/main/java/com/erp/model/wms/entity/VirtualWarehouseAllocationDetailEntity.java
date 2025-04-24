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
 * 虚拟仓分货单明细
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_allocation_detail")
public class VirtualWarehouseAllocationDetailEntity extends BaseEntity<VirtualWarehouseAllocationDetailEntity> {

    /**
    * 是否失效 true 失效 false 未失效
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 分货主单id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 调出虚拟仓id
    */
    @TableField("from_virtual_warehouse_id")
    private String fromVirtualWarehouseId;
    /**
    * 调出虚拟仓编码
    */
    @TableField("from_virtual_warehouse_code")
    private String fromVirtualWarehouseCode;
    /**
    * 调出虚拟仓名称
    */
    @TableField("from_virtual_warehouse_name")
    private String fromVirtualWarehouseName;
    /**
    * 调出数量/调拨数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 调入虚拟仓id
    */
    @TableField("to_virtual_warehouse_id")
    private String toVirtualWarehouseId;
    /**
    * 调入虚拟仓编码
    */
    @TableField("to_virtual_warehouse_code")
    private String toVirtualWarehouseCode;
    /**
    * 调出入虚拟仓名称
    */
    @TableField("to_virtual_warehouse_name")
    private String toVirtualWarehouseName;
    /**
    * 完结说明
    */
    @TableField("finish_description")
    private String finishDescription;
    /**
    * 第三方单据单号
    */
    @TableField("third_code")
    private String thirdCode;

    /**
     * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
     */
    @TableField("sync_status")
    private String syncStatus;
    /**
     * 同步平台
     */
    @TableField("sys_type")
    private String sysType;
    /**
     * 同步平台
     */
    @TableField("sys_type_name")
    private String sysTypeName;

    /**
     * 备注
     */
    @TableField("detail_remark")
    private String detailRemark;

    

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String FROM_VIRTUAL_WAREHOUSE_ID = "from_virtual_warehouse_id";

    public static final String FROM_VIRTUAL_WAREHOUSE_CODE = "from_virtual_warehouse_code";

    public static final String FROM_VIRTUAL_WAREHOUSE_NAME = "from_virtual_warehouse_name";

    

    public static final String TO_VIRTUAL_WAREHOUSE_ID = "to_virtual_warehouse_id";

    public static final String TO_VIRTUAL_WAREHOUSE_CODE = "to_virtual_warehouse_code";

    public static final String TO_VIRTUAL_WAREHOUSE_NAME = "to_virtual_warehouse_name";

    public static final String FINISH_DESCRIPTION = "finish_description";

    public static final String THIRD_CODE = "third_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}