package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * <p>
 * 中转报关表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare")
public class TransferDeclareEntity extends BaseEntity<TransferDeclareEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 预计中转日期
    */
    @TableField("plan_transfer_date")
    private LocalDate planTransferDate;
    /**
     * 入库预报日期
     */
    @TableField("instock_forecast_date")
    private LocalDate instockForecastDate;
    /**
    * 上传状态（批次）
    */
    @TableField("upload_status")
    private String uploadStatus;
    /**
    * 发货物流商id
    */
    @TableField("delivery_logistics_supplier_id")
    private String deliveryLogisticsSupplierId;
    /**
    * 发货物流商中文
    */
    @TableField("delivery_logistics_supplier_name")
    private String deliveryLogisticsSupplierName;
    /**
    * 中转物流商id
    */
    @TableField("transfer_logistics_supplier_id")
    private String transferLogisticsSupplierId;
    /**
    * 中转物流服务商
    */
    @TableField("transfer_logistics_supplier_name")
    private String transferLogisticsSupplierName;
    /**
    * 中转渠道id
    */
    @TableField("transfer_channel_id")
    private String transferChannelId;
    /**
    * 中转渠道中文
    */
    @TableField("transfer_channel_name")
    private String transferChannelName;
    /**
    * 包裹总数量
    */
    @TableField("package_total_qty")
    private Integer packageTotalQty;
    /**
    * 包裹总重量
    */
    @TableField("package_total_weight")
    private BigDecimal packageTotalWeight;

    /**
     * 入库预报状态(批次) dict_basic：type=instockForecastStatus
     */
    @TableField("instock_forecast_status")
    private String instockForecastStatus;

    /**
     * 入库预报asn单号
     */
    @TableField("instock_forecast_asn_code")
    private String instockForecastAsnCode;
    /**
     * 总件数（页面录入）
     */
    @TableField("total_qty")
    private Integer totalQty;
    /**
     * 入库成功/失败备注
     */
    @TableField("instock_forecast_remark")
    private String instockForecastRemark;
    /**
     * 入库预报客户参考单号
     */
    @TableField("instock_ref_code")
    private String instockRefCode;

    public static final String FIELD_CODE = "code";

    public static final String PLAN_TRANSFER_DATE = "plan_transfer_date";

    public static final String UPLOAD_STATUS = "upload_status";

    public static final String DELIVERY_LOGISTICS_SUPPLIER_ID = "delivery_logistics_supplier_id";

    public static final String DELIVERY_LOGISTICS_SUPPLIER_NAME = "delivery_logistics_supplier_name";

    public static final String TRANSFER_LOGISTICS_SUPPLIER_ID = "transfer_logistics_supplier_id";

    public static final String TRANSFER_LOGISTICS_SUPPLIER_NAME = "transfer_logistics_supplier_name";

    public static final String TRANSFER_CHANNEL_ID = "transfer_channel_id";

    public static final String TRANSFER_CHANNEL_NAME = "transfer_channel_name";

    public static final String PACKAGE_TOTAL_QTY = "package_total_qty";

    public static final String PACKAGE_TOTAL_WEIGHT = "package_total_weight";

    @Override
    public Serializable pkVal() {
        return null;
    }

}