package com.erp.model.wms.entity;

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
 * 订单销量关联表
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("report_order_sales_ref")
public class ReportOrderSalesRefEntity extends BaseEntity<ReportOrderSalesRefEntity> {

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
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 业务id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 业务编码
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 业务类型(取sourceTypeEnum枚举)
    */
    @TableField("business_type")
    private String businessType;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_TYPE = "business_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}