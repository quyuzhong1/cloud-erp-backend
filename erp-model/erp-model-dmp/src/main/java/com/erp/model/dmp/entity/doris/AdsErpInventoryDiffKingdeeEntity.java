package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 金蝶库存差异
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ads_erp_inventory_diff_kingdee")
public class AdsErpInventoryDiffKingdeeEntity extends BaseEntity<AdsErpInventoryDiffKingdeeEntity> {

    /**
    * 数据唯一md5值
    */
    @TableField("unique_code")
    private String uniqueCode;
    /**
    * 数据字段md5值（数据json+account_code+next_level_id+bill_topic）
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 平台账号编码
    */
    @TableField("account_code")
    private String accountCode;
    /**
    * 店铺ID/海外仓授权ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 业务类型
    */
    @TableField("bill_topic")
    private String billTopic;
    /**
    * 流程id
    */
    @TableField("flow_id")
    private String flowId;
    /**
    * ETL处理状态：ready=可处理，unready=不可处理
    */
    @TableField("etl_status")
    private String etlStatus;
    /**
    * 节点id
    */
    @TableField("node_id")
    private String nodeId;
    /**
    * 实例id/etl任务id
    */
    @TableField("instance_id")
    private String instanceId;
    /**
    * 拉取任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 最后一次当前数据是否变更（TRUE=已变更）
    */
    @TableField("last_update_flag")
    private String lastUpdateFlag;
    /**
    * 核对周期（YYYY-MM）
    */
    @TableField("check_month")
    private String checkMonth;
    /**
    * 核对仓库ID
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 核对仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
    * 核对系统仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 核对SKU_ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 核对SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 核对产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * ERP期末在库
    */
    @TableField("closing_qty")
    private Integer closingQty;
    /**
    * ERP期末在途
    */
    @TableField("intransit_qty")
    private Integer intransitQty;
    /**
    * 金蝶仓库ID
    */
    @TableField("kingdee_warehouse_id")
    private String kingdeeWarehouseId;
    /**
    * 金蝶仓库编码
    */
    @TableField("kingdee_warehouse_code")
    private String kingdeeWarehouseCode;
    /**
    * 金蝶仓库名称
    */
    @TableField("kingdee_warehouse_name")
    private String kingdeeWarehouseName;
    /**
    * 金蝶仓位编码
    */
    @TableField("kingdee_warehouse_location")
    private String kingdeeWarehouseLocation;
    /**
    * 金蝶仓位名称
    */
    @TableField("kingdee_warehouse_location_name")
    private String kingdeeWarehouseLocationName;
    /**
    * 金蝶SKU_ID
    */
    @TableField("kingdee_sku_id")
    private String kingdeeSkuId;
    /**
    * 金蝶SKU编码
    */
    @TableField("kingdee_sku_no")
    private String kingdeeSkuNo;
    /**
    * 金蝶产品名称
    */
    @TableField("kingdee_product_name")
    private String kingdeeProductName;
    /**
    * 货主
    */
    @TableField("kingdee_owner")
    private String kingdeeOwner;
    /**
    * 金蝶期末在库库存
    */
    @TableField("kingdee_closing_qty")
    private Integer kingdeeClosingQty;
    /**
    * 金蝶本期在库出库数量
    */
    @TableField("kingdee_outstock_qty")
    private Integer kingdeeOutstockQty;
    /**
    * 金蝶本期在库入库数量
    */
    @TableField("kingdee_instock_qty")
    private Integer kingdeeInstockQty;
    /**
    * 金蝶期末在途库存
    */
    @TableField("kingdee_closing_intransit_qty")
    private Integer kingdeeClosingIntransitQty;
    /**
    * 金蝶本期在途出库数量
    */
    @TableField("kingdee_outstock_intransit_qty")
    private Integer kingdeeOutstockIntransitQty;
    /**
    * 金蝶本期在途入库数量
    */
    @TableField("kingdee_instock_intransit_qty")
    private Integer kingdeeInstockIntransitQty;
    /**
    * 期末在途差异数量
    */
    @TableField("diff_closing_intransit_qty")
    private Integer diffClosingIntransitQty;
    /**
    * 期末在库差异数量
    */
    @TableField("diff_closing_qty")
    private Integer diffClosingQty;
    /**
    * 差异说明备注
    */
    @TableField("remark")
    private String remark;


    public static final String UNIQUE_CODE = "unique_code";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String ACCOUNT_CODE = "account_code";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String BILL_TOPIC = "bill_topic";

    public static final String FLOW_ID = "flow_id";

    public static final String ETL_STATUS = "etl_status";

    public static final String NODE_ID = "node_id";

    public static final String INSTANCE_ID = "instance_id";

    public static final String TASK_ID = "task_id";

    public static final String SOURCE_ID = "source_id";

    public static final String LAST_UPDATE_FLAG = "last_update_flag";

    public static final String check_month = "check_month";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String CLOSING_QTY = "closing_qty";

    public static final String INTRANSIT_QTY = "intransit_qty";

    public static final String KINGDEE_WAREHOUSE_ID = "kingdee_warehouse_id";

    public static final String KINGDEE_WAREHOUSE_CODE = "kingdee_warehouse_code";

    public static final String KINGDEE_WAREHOUSE_NAME = "kingdee_warehouse_name";

    public static final String KINGDEE_WAREHOUSE_LOCATION = "kingdee_warehouse_location";

    public static final String KINGDEE_WAREHOUSE_LOCATION_NAME = "kingdee_warehouse_location_name";

    public static final String KINGDEE_SKU_ID = "kingdee_sku_id";

    public static final String KINGDEE_SKU_NO = "kingdee_sku_no";

    public static final String KINGDEE_PRODUCT_NAME = "kingdee_product_name";

    public static final String KINGDEE_OWNER = "kingdee_owner";

    public static final String KINGDEE_CLOSING_QTY = "kingdee_closing_qty";

    public static final String KINGDEE_OUTSTOCK_QTY = "kingdee_outstock_qty";

    public static final String KINGDEE_INSTOCK_QTY = "kingdee_instock_qty";

    public static final String KINGDEE_CLOSING_INTRANSIT_QTY = "kingdee_closing_intransit_qty";

    public static final String KINGDEE_OUTSTOCK_INTRANSIT_QTY = "kingdee_outstock_intransit_qty";

    public static final String KINGDEE_INSTOCK_INTRANSIT_QTY = "kingdee_instock_intransit_qty";

    public static final String DIFF_CLOSING_INTRANSIT_QTY = "diff_closing_intransit_qty";

    public static final String DIFF_CLOSING_QTY = "diff_closing_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}