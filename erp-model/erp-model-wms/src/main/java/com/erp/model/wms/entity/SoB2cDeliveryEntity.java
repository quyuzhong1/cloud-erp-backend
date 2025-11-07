package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * <p>
 * b2c发货单
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_delivery")
public class SoB2cDeliveryEntity extends BaseEntity<SoB2cDeliveryEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 状态 waitHandle:待处理  picking:拣货中 falseShipment:手动标发 shipped:已发货  cancelDelivery:取消发货
    */
    @TableField("status")
    private String status;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
     * 平台订单号
     */
    @TableField("platform_code")
    private String platformCode;

    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
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
    * 拣货类型
    */
    @TableField("picking_type")
    private String pickingType;
    /**
    * 是否打印拣货单
    */
    @TableField("is_print_picking")
    private Boolean isPrintPicking;
    /**
     * 是否打印SKU条码
     */
    @TableField("is_print_sku_barcode")
    private Boolean isPrintSkuBarcode;

    /**
     * 是否打印物流单
     */
    @TableField("is_print_logistic")
    private Boolean isPrintLogistic;

    /**
     * 完成打印时间
     */
    @TableField(value = "finish_print_time",updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime finishPrintTime;
    /**
    * 是否验货
    */
    @TableField("is_inspection")
    private Boolean isInspection;
    /**
     * 验货时间
     */
    @TableField(value = "inspection_time",updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime inspectionTime;
    /**
    * 是否称重
    */
    @TableField("is_weigh")
    private Boolean isWeigh;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 称重重量
    */
    @TableField("weight")
    private BigDecimal weight;
    /**
     * 称重时间
     */
    @TableField(value = "weighing_time",updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime weighingTime;

    /**
    * 单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 物流类型 oms的dict_basic表type=orderLogisticType
    */
    @TableField("logistic_type")
    private String logisticType;

    /**
     * 异常原因
     */
    @TableField("abnormal_cause")
    private String abnormalCause;

    /**
     * 是否自动出库
     */
    @TableField("is_auto_out")
    private Boolean isAutoOut;

    /**
     * 长，cm
     */
    @TableField("length")
    private BigDecimal length;
    /**
     * 宽，cm
     */
    @TableField("width")
    private BigDecimal width;
    /**
     * 高，cm
     */
    @TableField("height")
    private BigDecimal height;

    /**
     * 发货标记类型
     */
    @TableField("shipment_mark")
    private String shipmentMark;
    /**
     * 中转仓库 以,分割记录
     */
    @TableField("transfer_warehouse_ids")
    private String transferWarehouseIds;
    /**
     * 批次号
     */
    @TableField(exist = false)
    private String batchNo;


    /**
     * 同类波次标识
     */
    @TableField(exist = false)
    private String sameWaveStr;

    /**
     * 是否标记不出库发货（默认false）
     */
    @TableField(exist = false)
    private Boolean isNotOutbound;
    

    

    public static final String SO_CODE = "so_code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String PICKING_TYPE = "picking_type";

    public static final String IS_PRINT_PICKING = "is_print_picking";

    public static final String IS_INSPECTION = "is_inspection";

    public static final String IS_WEIGH = "is_weigh";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String DELIVERY_TIME = "delivery_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}