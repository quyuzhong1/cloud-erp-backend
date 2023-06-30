package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * FBA发货单
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_delivery")
public class DmpFbaDeliveryEntity extends BaseEntity<DmpFbaDeliveryEntity> {


    /**
    * 物流方式名称
    */
    @TableField("logic_name")
    private String logicName;

    /**
    * 清关时间
    */
    @TableField("customs_clearance_time")
    private String customsClearanceTime;

    /**
    * 总发货量
    */
    @TableField("total_apply_quantity")
    private Integer totalApplyQuantity;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private String deliveryTime;

    /**
    * 完结标识  1：完结 2：未完结
    */
    @TableField("is_over")
    private Integer isOver;

    /**
    * 物流单价
    */
    @TableField("logic_price")
    private BigDecimal logicPrice;

    /**
    * 预计到港时间
    */
    @TableField("estimate_time")
    private String estimateTime;

    /**
    * 发货单类型 1:手动发货,2:转wms发货单
    */
    @TableField("delivery_type")
    private Integer deliveryType;

    /**
    * 发货单id
    */
    @TableField("delivery_id")
    private String deliveryId;

    /**
    * 单个发货单的商品数
    */
    @TableField("stock_sum")
    private Integer stockSum;

    /**
    * 总重量
    */
    @TableField("total_weights")
    private BigDecimal totalWeights;

    /**
    * 渠道名称
    */
    @TableField("channel_name")
    private String channelName;

    /**
    * 开船时间
    */
    @TableField("sail_time")
    private String sailTime;

    /**
    * 到货时间
    */
    @TableField("arrival_time")
    private String arrivalTime;

    /**
    * 总体积
    */
    @TableField("total_volumes")
    private BigDecimal totalVolumes;

    /**
    * 用户名称
    */
    @TableField("employee_name")
    private String employeeName;

    /**
    * 物流中心编码
    */
    @TableField("logistics_code")
    private String logisticsCode;

    /**
    * 发货单号
    */
    @TableField("delivery_no")
    private String deliveryNo;

    /**
    * 总费用
    */
    @TableField("extend_fee")
    private BigDecimal extendFee;

    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
    * 渠道id
    */
    @TableField("channel_id")
    private String channelId;

    /**
    * 发货单状态
    */
    @TableField("delivery_status")
    private Integer deliveryStatus;

    /**
    * fba仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    @TableField(exist = false)
    private List<DmpFbaDeliveryDetailEntity> itemList;


    public static final String LOGIC_NAME = "logic_name";

    public static final String CUSTOMS_CLEARANCE_TIME = "customs_clearance_time";

    public static final String TOTAL_APPLY_QUANTITY = "total_apply_quantity";

    public static final String REMARK = "remark";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String IS_OVER = "is_over";

    public static final String LOGIC_PRICE = "logic_price";

    public static final String ESTIMATE_TIME = "estimate_time";

    public static final String DELIVERY_TYPE = "delivery_type";

    public static final String DELIVERY_ID = "delivery_id";

    public static final String STOCK_SUM = "stock_sum";

    public static final String TOTAL_WEIGHTS = "total_weights";

    public static final String CHANNEL_NAME = "channel_name";

    public static final String SAIL_TIME = "sail_time";

    public static final String ARRIVAL_TIME = "arrival_time";

    public static final String TOTAL_VOLUMES = "total_volumes";

    public static final String EMPLOYEE_NAME = "employee_name";

    public static final String LOGISTICS_CODE = "logistics_code";

    public static final String DELIVERY_NO = "delivery_no";

    public static final String EXTEND_FEE = "extend_fee";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String CHANNEL_ID = "channel_id";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String PLATFORM_SIGN = "platform_sign";

    @Override
    public Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "DmpFbaDeliveryEntity{" +
                "logicName='" + logicName + '\'' +
                ", customsClearanceTime='" + customsClearanceTime + '\'' +
                ", totalApplyQuantity=" + totalApplyQuantity +
                ", remark='" + remark + '\'' +
                ", deliveryTime='" + deliveryTime + '\'' +
                ", isOver=" + isOver +
                ", logicPrice=" + logicPrice +
                ", estimateTime='" + estimateTime + '\'' +
                ", deliveryType=" + deliveryType +
                ", deliveryId='" + deliveryId + '\'' +
                ", stockSum=" + stockSum +
                ", totalWeights=" + totalWeights +
                ", channelName='" + channelName + '\'' +
                ", sailTime='" + sailTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", totalVolumes=" + totalVolumes +
                ", employeeName='" + employeeName + '\'' +
                ", logisticsCode='" + logisticsCode + '\'' +
                ", deliveryNo='" + deliveryNo + '\'' +
                ", extendFee=" + extendFee +
                ", warehouseName='" + warehouseName + '\'' +
                ", channelId='" + channelId + '\'' +
                ", deliveryStatus=" + deliveryStatus +
                ", warehouseId='" + warehouseId + '\'' +
                ", platformSign='" + platformSign + '\'' +
                ", itemList=" + itemList +
                '}';
    }
}