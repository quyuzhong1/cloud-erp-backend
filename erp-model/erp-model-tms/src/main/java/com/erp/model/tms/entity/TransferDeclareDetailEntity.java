package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 中转报关详情
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_detail")
public class TransferDeclareDetailEntity extends BaseEntity<TransferDeclareDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 销售单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道中文
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 包裹重量
    */
    @TableField("package_weight")
    private BigDecimal packageWeight;
    /**
    * 包裹重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 出库状态 dict_basic：type=transferOutstockStatus
    */
//    @TableField("outstock_status")
//    private String outstockStatus;
    /**
     * 出库状态名称
     */
    @TableField(exist = false)
    private String outstockStatusName;
    /**
    * 中转状态 dict_basic：type=transferStatus
     * enum :TransferLogisticsStatusEnum
    */
    @TableField("transfer_status")
    private String transferStatus;
    /**
     * 中转状态名称
     */
    @TableField(exist = false)
    private String transferStatusName;
    /**
     * 上传状态（订单）dict_basic：type=transferDeclareUploadStatus
     */
    @TableField("order_upload_status")
    private String orderUploadStatus;
    /**
     * 上传状态名称
     */
    @TableField(exist = false)
    private String orderUploadStatusName;
    /**
     * 失败原因
     */
    @TableField("failure_reason")
    private String failureReason;
    /**
     * 第三方中转服务商的发货单号
     */
    @TableField("shipping_order_no")
    private String shippingOrderNo;
    /**
     * 组包单号
     */
    @TableField(exist = false)
    private String packageForecastCode;
    /**
     * 平台订单编号
     */
    @TableField(exist = false)
    private String platformOrderCode;


    public static final String MAIN_ID = "main_id";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRACKING_NO = "tracking_no";

    public static final String PACKAGE_WEIGHT = "package_weight";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String OUTSTOCK_STATUS = "outstock_status";

    public static final String TRANSFER_STAUS = "transfer_staus";

    @Override
    public Serializable pkVal() {
        return null;
    }

}