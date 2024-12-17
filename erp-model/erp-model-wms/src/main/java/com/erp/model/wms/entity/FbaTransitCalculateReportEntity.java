package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * FBA在途核算报表
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_transit_calculate_report")
public class FbaTransitCalculateReportEntity extends BaseEntity<FbaTransitCalculateReportEntity> {

    /**
    * 月份（YYYY-MM）
    */
    @TableField("report_month")
    private LocalDate reportMonth;
    /**
    * 货件ID
    */
    @TableField("shipment_id")
    private String shipmentId;
    /**
    * 货件单号
    */
    @TableField("shipment_code")
    private String shipmentCode;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
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
    * 客户id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 客户姓名
    */
    @TableField("customer_name")
    private String customerName;
    /**
    * 货件状态
    */
    @TableField("shipment_status")
    private String shipmentStatus;
    /**
    * 货件创建时间 货件主表
    */
    @TableField("shipment_create_time")
    private LocalDateTime shipmentCreateTime;
    /**
    * 货件签收时间 货件主表
    */
    @TableField("shipment_receive_time")
    private LocalDateTime shipmentReceiveTime;

    public static final String REPORT_MONTH = "report_month";

    public static final String SHIPMENT_ID = "shipment_id";

    public static final String SHIPMENT_CODE = "shipment_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String SHIPMENT_STATUS = "shipment_status";

    public static final String SHIPMENT_CREATE_TIME = "shipment_create_time";

    public static final String SHIPMENT_RECEIVE_TIME = "shipment_receive_time";

    public static final String TRANSIT_ADJUST_TIME = "transit_adjust_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}