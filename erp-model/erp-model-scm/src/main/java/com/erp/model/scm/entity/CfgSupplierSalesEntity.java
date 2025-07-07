package com.erp.model.scm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 销量设置
 * </p>
 *
 * @author jack
 * @since 2025-06-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_supplier_sales")
public class CfgSupplierSalesEntity extends BaseEntity<CfgSupplierSalesEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商编码
    */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 页面权限：view=仅查看,download=查看并下载  枚举：CfgSupplierSalesPermissionEnum
    */
    @TableField("permission")
    private String permission;
    /**
    * 日均销量类型：dailyAvg3Days=按3天日均计算,dailyAvg7Days=按7天日均计算,dailyAvg30Days=按30天日均计算,dailyAvg60Days=按60天日均计算,dailyAvg90Days=按90天日均计算  枚举：CfgSupplierSalesDailySalesTypeEnum
    */
    @TableField("daily_sales_type")
    private String dailySalesType;
    /**
    * 销量比例类型：purchaseRatio=按照供应商采购比例,salesStatisticRatio=按照销量统计比例  枚举：CfgSupplierSalesSalesRatioTypeEnum
    */
    @TableField("sales_ratio_type")
    private String salesRatioType;
    /**
    * 销量比例值
    */
    @TableField("sales_ratio")
    private BigDecimal salesRatio;
    /**
    * 是否启用通知
    */
    @TableField("notice_enabled")
    private Boolean noticeEnabled;
    /**
    * 统计维度：deliveryTime=按照出库时间,paymentTime=按照付款时间  枚举：CfgSupplierSalesDimensionEnum
    */
    @TableField("dimension")
    private String dimension;
    /**
    * 禁用状态
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 字段显示 CfgSupplierSalesDisplayFieldEnum
    */
    @TableField("display_field")
    private String displayField;

    /**
     * 仓库类型：virtualWarehouse=虚拟仓,physicalWarehouse=实体仓  枚举：CfgSupplierSalesConditionWarehouseTypeEnum
     */
    @TableField("warehouse_type")
    private String warehouseType;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String PERMISSION = "permission";

    public static final String DAILY_SALES_TYPE = "daily_sales_type";

    public static final String SALES_RATIO_TYPE = "sales_ratio_type";

    public static final String SALES_RATIO = "sales_ratio";

    public static final String NOTICE_ENABLED = "notice_enabled";

    public static final String DIMENSION = "dimension";

    public static final String DISABLED = "disabled";

    public static final String DISPLAY_FIELD = "display_field";

    public static final String WAREHOUSE_TYPE = "warehouse_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
