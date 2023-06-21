package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 直接调拨详情
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_transfer_info_detail")
public class DmpTransferInfoDetailEntity extends BaseEntity<DmpTransferInfoDetailEntity> {


    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;

    /**
    * 调入物料编码
    */
    @TableField("in_sku_no")
    private String inSkuNo;

    /**
    * 调入物料名称
    */
    @TableField("in_product_name")
    private String inProductName;

    /**
    * 单位
    */
    @TableField("unit")
    private String unit;

    /**
    * 调拨数量
    */
    @TableField("qty")
    private Integer qty;

    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
    * 入库时间
    */
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    /**
    * 入库库存状态编码
    */
    @TableField("in_stock_status_code")
    private String inStockStatusCode;

    /**
    * 入库库存状态名称
    */
    @TableField("in_stock_status_name")
    private String inStockStatusName;

    /**
    * 出库库存状态编码
    */
    @TableField("out_stock_status_code")
    private String outStockStatusCode;

    /**
    * 出库库存状态名称
    */
    @TableField("out_stock_status_name")
    private String outStockStatusName;

    /**
    * 调出物料编码
    */
    @TableField("out_sku_no")
    private String outSkuNo;

    /**
    * 调出物料名称
    */
    @TableField("out_product_name")
    private String outProductName;

    /**
    * 调入仓库code
    */
    @TableField("in_warehouse_code")
    private String inWarehouseCode;

    /**
    * 调入仓库名称
    */
    @TableField("in_warehouse_name")
    private String inWarehouseName;

    /**
    * 调出仓库code
    */
    @TableField("out_warehouse_code")
    private String outWarehouseCode;

    /**
    * 调出仓库名称
    */
    @TableField("out_warehouse_name")
    private String outWarehouseName;


    public static final String MAIN_ID = "main_id";

    public static final String IN_SKU_NO = "in_sku_no";

    public static final String IN_PRODUCT_NAME = "in_product_name";

    public static final String UNIT = "unit";

    public static final String QTY = "qty";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String RECEIVE_TIME = "receive_time";

    public static final String IN_STOCK_STATUS_CODE = "in_stock_status_code";

    public static final String IN_STOCK_STATUS_NAME = "in_stock_status_name";

    public static final String OUT_STOCK_STATUS_CODE = "out_stock_status_code";

    public static final String OUT_STOCK_STATUS_NAME = "out_stock_status_name";

    public static final String OUT_SKU_NO = "out_sku_no";

    public static final String OUT_PRODUCT_NAME = "out_product_name";

    public static final String IN_WAREHOUSE_CODE = "in_warehouse_code";

    public static final String IN_WAREHOUSE_NAME = "in_warehouse_name";

    public static final String OUT_WAREHOUSE_CODE = "out_warehouse_code";

    public static final String OUT_WAREHOUSE_NAME = "out_warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}