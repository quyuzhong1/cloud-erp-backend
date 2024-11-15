package com.erp.model.wms.entity;

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
 * 组包预报表
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("package_forecast")
public class PackageForecastEntity extends BaseEntity<PackageForecastEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 第三方交接单号
    */
    @TableField("handover_no")
    private String handoverNo;
    /**
    * 第三方组包id 对应 handoverContentId
    */
    @TableField("platform_package_no")
    private String platformPackageNo;
    /**
    * 物流商id
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流商名
    */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;
    /**
    * 包裹总数量
    */
    @TableField("total_package_qty")
    private Integer totalPackageQty;
    /**
    * 包裹总重量
    */
    @TableField("total_package_weight")
    private BigDecimal totalPackageWeight;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 运输单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 上传状态
    */
    @TableField("upload_status")
    private String uploadStatus;
    /**
    * 打印状态
    */
    @TableField("print_status")
    private String printStatus;
    /**
    * 组包日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 揽收方式
    */
    @TableField("collect_mode")
    private String collectMode;
    /**
    * 揽收地址
    */
    @TableField("collect_address")
    private String collectAddress;
    /**
    * 揽收地址id logistics_address
    */
    @TableField("collect_address_id")
    private String collectAddressId;
    /**
    * 第三方交接状态
    */
    @TableField("handover_status")
    private String handoverStatus;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    @TableField("transfer_status")
    private String transferStatus;


    public static final String HANDOVER_NO = "handover_no";

    public static final String PLATFORM_PACKAGE_NO = "platform_package_no";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String TOTAL_PACKAGE_QTY = "total_package_qty";

    public static final String TOTAL_PACKAGE_WEIGHT = "total_package_weight";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String UPLOAD_STATUS = "upload_status";

    public static final String PRINT_STATUS = "print_status";

    public static final String BILL_DATE = "bill_date";

    public static final String COLLECT_MODE = "collect_mode";

    public static final String COLLECT_ADDRESS = "collect_address";

    public static final String COLLECT_ADDRESS_ID = "collect_address_id";

    public static final String HANDOVER_STATUS = "handover_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}