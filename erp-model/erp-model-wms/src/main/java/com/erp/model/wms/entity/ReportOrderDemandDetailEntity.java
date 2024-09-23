package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 订单需求明细报表
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
    private ApproveStatusEnum approveStatus;
    /**
    * 单据状态
    */
    @TableField("status")
    private String status;
    /**
    * 作废状态
    */
    @TableField("invaild_status")
    private Boolean invaildStatus;
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
    @TableField("souce_code")
    private String souceCode;
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


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String QTY = "qty";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String STATUS = "status";

    public static final String INVAILD_STATUS = "invaild_status";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOUCE_CODE = "souce_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String DATE = "date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}