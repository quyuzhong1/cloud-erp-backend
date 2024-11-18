package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 预报设置
 * </p>
 *
 * @author Lambda
 * @since 2024-01-18
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("setting_forecast")
@EqualsAndHashCode
public class SettingForecastEntity extends BaseEntity<SettingForecastEntity> {

    /**
     * 物流商id
     */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;

    /**
     * 物流商
     */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;

    /**
     * 是否强制组包 true 
     */
    @TableField("is_must_package")
    private Boolean isMustPackage;

    /**
     * 组包启用时间
     */
    @TableField( value= "enable_package_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime enablePackageTime;

    /**
     * 是否强制中转  true 是
     */
    @TableField("is_must_transfer")
    private Boolean isMustTransfer;

    /**
     * 中转时间启用时间
     */
    @TableField( value = "enable_transfer_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime enableTransferTime;

    /**
     * 报关平台
     */
    @TableField("declare_platform")
    private String declarePlatform;


    /**
     * 中转物流商id
     */
    @TableField("transfer_logistics_supplier_id")
    private String transferLogisticsSupplierId;

    /**
     * 中转物流商名
     */
    @TableField("transfer_logistics_supplier_name")
    private String transferLogisticsSupplierName;


    /**
     * 中转商渠道id
     */
    @TableField("transfer_logistics_channel_id")
    private String transferLogisticsChannelId;

    /**
     * 中转商渠道名
     */
    @TableField("transfer_logistics_channel_name")
    private String transferLogisticsChannelName;

    /**
     * 是否自动预报  true 是
     */
    @TableField("is_auto_forecast")
    private Boolean isAutoForecast;

    /**
     * 物流渠道id集合
     */
    @TableField(exist = false)
    private List<String> logisticsChannelIdList;

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String IS_MUST_PACKAGE = "is_must_package";

    public static final String ENABLE_PACKAGE_TIME = "enable_package_time";

    public static final String IS_MUST_TRANSFER = "is_must_transfer";

    public static final String ENABLE_TRANSFER_TIME = "enable_transfer_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
