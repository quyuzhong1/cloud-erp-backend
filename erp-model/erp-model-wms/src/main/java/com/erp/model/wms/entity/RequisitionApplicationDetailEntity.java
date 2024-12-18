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
 * 要货申请单明细表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("requisition_application_detail")
public class RequisitionApplicationDetailEntity extends BaseEntity<RequisitionApplicationDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;
    /**
    * 要货数量
    */
    @TableField("requisition_qty")
    private Integer requisitionQty;
    /**
    * 批准数量
    */
    @TableField("approve_qty")
    private Integer approveQty;
    /**
    * 拣货数量
    */
    @TableField("picking_qty")
    private Integer pickingQty;

    /**
     * 虚拟仓冻结数量
     */
    @TableField("virtual_frozen_qty")
    private Integer virtualFrozenQty;
    /**
     * 来源详情id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
     * 调入仓库id
     */
    @TableField("to_warehouse_id")
    private String toWarehouseId;
    /**
     * 调入仓库中文名
     */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
    /**
     * 调出仓库id
     */
    @TableField("from_warehouse_id")
    private String fromWarehouseId;
    /**
     * 调出仓库中文名
     */
    @TableField("from_warehouse_name")
    private String fromWarehouseName;
    /**
     * 调出仓库id
     */
    @TableField("from_virtual_warehouse_id")
    private String fromVirtualWarehouseId;
    /**
     * 调出仓库中文名
     */
    @TableField("from_virtual_warehouse_name")
    private String fromVirtualWarehouseName;
    /**
     * 要货仓位
     */
    @TableField("requisition_warehouse_location")
    private String requisitionWarehouseLocation;

    /**
     * 平台sku
     */
    @TableField("platform_sku")
    private String platformSku;

    /**
     * 平台sku
     */
    @TableField("platform_sku_name")
    private String platformSkuName;

    /**
     * 平台spu
     */
    @TableField("platform_spu")
    private String platformSpu;

    /**
     * fnSku
     */
    @TableField("platform_fn_sku")
    private String platformFnSku;

    @TableField(exist = false)
    private Integer changeBeforeQty;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String BOM_VERSION = "bom_version";

    public static final String REQUISITION_QTY = "requisition_qty";

    public static final String APPROVE_QTY = "approve_qty";

    public static final String PICKING_QTY = "picking_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}