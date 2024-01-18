package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
    @TableField("enable_package_time")
    private Date enablePackageTime;

    /**
     * 是否强制中转  true 是
     */
    @TableField("is_must_transfer")
    private Boolean isMustTransfer;

    /**
     * 中转时间启用时间
     */
    @TableField("enable_transfer_time")
    private Date enableTransferTime;


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
