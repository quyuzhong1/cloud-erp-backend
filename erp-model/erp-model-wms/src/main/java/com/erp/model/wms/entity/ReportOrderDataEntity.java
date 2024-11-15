package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


/**
 * <p>
 * 订单报表信息
 * </p>
 *
 * @author will
 * @since 2024-09-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("report_order_data")
public class ReportOrderDataEntity extends BaseEntity<ReportOrderDataEntity> {

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
    * sku id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private String approveStatus;
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
    * 时间
    */
    @TableField("date")
    private LocalDate date;

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
     * 冻结数量
     */
    @TableField("frozen_qty")
    private Integer frozenQty;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ReportOrderDataEntity that = (ReportOrderDataEntity) o;
        return Objects.equals(warehouseId, that.warehouseId) && Objects.equals(virtualWarehouseId, that.virtualWarehouseId) && Objects.equals(skuId, that.skuId) && Objects.equals(qty, that.qty) && approveStatus == that.approveStatus && Objects.equals(status, that.status) && Objects.equals(invalidStatus, that.invalidStatus) && Objects.equals(sourceId, that.sourceId) && Objects.equals(sourceDetailId, that.sourceDetailId) && Objects.equals(sourceCode, that.sourceCode) && Objects.equals(sourceType, that.sourceType) && Objects.equals(date, that.date) && Objects.equals(deliveryNoticeQty, that.deliveryNoticeQty) && Objects.equals(frozenQty, that.frozenQty);
    }

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    

    public static final String APPROVE_STATUS = "approve_status";

    

    public static final String INVALID_STATUS = "invalid_status";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}