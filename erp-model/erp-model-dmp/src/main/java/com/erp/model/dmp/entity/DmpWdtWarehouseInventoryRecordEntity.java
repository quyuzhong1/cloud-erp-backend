package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 旺店通库存同步记录
 * </p>
 *
 * @author zdy
 * @since 2026-01-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_wdt_warehouse_inventory_record")
public class DmpWdtWarehouseInventoryRecordEntity extends BaseEntity<DmpWdtWarehouseInventoryRecordEntity> {

    /**
     * 订单类型 inStock 入库单，outStock出库单
     * InventoryOrderTypeEnum
     */
    @TableField("order_type")
    private String orderType;
    /**
     * 来源平台（编码）：goodcang、iml
     */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
     * 单据同步状态 init初始状态 success成功 failed失败
     * InventoryBillStatusEnum
     */
    @TableField("bill_status")
    private String billStatus;
    /**
     * erp仓库id
     */
    @TableField("erp_warehouse_id")
    private String erpWarehouseId;
    /**
     * erp仓库名称
     */
    @TableField("erp_warehouse_name")
    private String erpWarehouseName;
    /**
     * erp产品编码
     */
    @TableField("erp_sku_no")
    private String erpSkuNo;
    /**
     * erp产品ID
     */
    @TableField("erp_sku_id")
    private String erpSkuId;
    /**
     * ERP可用库存数量
     */
    @TableField("erp_usable_qty")
    private Integer erpUsableQty;
    /**
     * 第三方仓库ID
     */
    @TableField("third_warehouse_id")
    private String thirdWarehouseId;
    /**
     * 第三方仓库编码
     */
    @TableField("third_warehouse_code")
    private String thirdWarehouseCode;
    /**
     * 第三方仓库名称
     */
    @TableField("third_warehouse_name")
    private String thirdWarehouseName;
    /**
     * 第三方产品编码
     */
    @TableField("third_sku_no")
    private String thirdSkuNo;
    /**
     * 第三方仓总库存
     */
    @TableField("third_stock_qty")
    private Integer thirdStockQty;
    /**
     * 第三方冻结库存
     */
    @TableField("third_freeze_qty")
    private Integer thirdFreezeQty;
    /**
     * 第三方可用库存
     */
    @TableField("third_usable_qty")
    private Integer thirdUsableQty;
    /**
     * 差异库存
     */
    @TableField("qty")
    private Integer qty;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 唯一值
     */
    @TableField("pkey")
    private String pkey;
    /**
     * 批次编码
     */
    @TableField("batch_no")
    private String batchNo;
    /**
     * 输入任务id
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 转换id
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 下一层级id
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 唯一字段md5值
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 数据字段md5值
     */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String ORDER_TYPE = "order_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String BIIL_STATUS = "biil_status";

    public static final String ERP_WAREHOUSE_ID = "erp_warehouse_id";

    public static final String ERP_WAREHOUSE_NAME = "erp_warehouse_name";

    public static final String ERP_SKU_NO = "erp_sku_no";

    public static final String ERP_SKU_ID = "erp_sku_id";

    public static final String ERP_USABLE_QTY = "erp_usable_qty";

    public static final String THIRD_WAREHOUSE_ID = "third_warehouse_id";

    public static final String THIRD_WAREHOUSE_CODE = "third_warehouse_code";

    public static final String THIRD_WAREHOUSE_NAME = "third_warehouse_name";

    public static final String THIRD_SKU_NO = "third_sku_no";

    public static final String THIRD_STOCK_QTY = "third_stock_qty";

    public static final String THIRD_FREEZE_QTY = "third_freeze_qty";

    public static final String THIRD_USABLE_QTY = "third_usable_qty";

    public static final String QTY = "qty";

    public static final String REMARK = "remark";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}