package com.erp.model.wms.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("report_order_demand_detail")
public class ReportOrderDemandDetailEntity extends BaseEntity<ReportOrderDemandDetailEntity> {

    /**
    * 仓库id 
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓库id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 虚拟出库名称
    */
    @TableField("virtual_warehouse_name")
    private String virtualWarehouseName;
    /**
    * 实体仓名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 剩余需求总数
    */
    @TableField("qty")
    private Integer qty;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
     * 单据状态
     */
    @TableField("status")
    private String status;
    /**
     * 作废状态
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 虚拟仓可用库存
    */
    @TableField("virtual_usable_qty")
    private Integer virtualUsableQty;

    /**
     * 来源单据id
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 来源单据明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
     * 来源单据编号
     */
    @TableField("source_code")
    private String sourceCode;
    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 是否拆分
     */
    @TableField("is_split")
    private Boolean isSplit;

    /**
     * 订单数量
     */
    @TableField("order_qty")
    private Integer orderQty;

    /**
     * 发货通知单
     */
    @TableField("delivery_notice_qty")
    private Integer deliveryNoticeQty;

    /**
     * 发货通知单
     */
    @TableField("frozen_qty")
    private Integer frozenQty;

    /**
     * bom的json数据
     */
    @TableField(value = "bom_json", javaType = true )
    private JSONArray bomJson;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_NAME = "virtual_warehouse_name";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String VIRTUAL_USABLE_QTY = "virtual_usable_qty";


    @Override
    public Serializable pkVal() {
        return null;
    }

}