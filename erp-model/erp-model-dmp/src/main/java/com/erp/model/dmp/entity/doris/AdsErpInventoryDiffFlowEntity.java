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
 * 第三方仓流水差异表
 * </p>
 *
 * @author shukai
 * @since 2025-11-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ads_erp_inventory_diff_flow")
public class AdsErpInventoryDiffFlowEntity extends BaseEntity<AdsErpInventoryDiffFlowEntity> {

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
    * 数据来源平台
    */
    @TableField("source_platform")
    private String sourcePlatform;
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
    * 任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * ETL处理状态：ready=可处理，unready=不可处理
    */
    @TableField("etl_status")
    private String etlStatus;
    /**
    * 核对唯一键
    */
    @TableField("check_key")
    private String checkKey;
    /**
    * 核对周期
    */
    @TableField("check_month")
    private String checkMonth;
    /**
    * 核对仓库
    */
    @TableField("source_system_name")
    private String sourceSystemName;
    /**
    * 出库仓库编码
    */
    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;
    /**
    * 出库仓库名称
    */
    @TableField("platform_warehouse_name")
    private String platformWarehouseName;
    /**
    * ERP仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
    * ERP仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 库存SKU
    */
    @TableField("stock_sku")
    private String stockSku;
    /**
    * ERP_SKU_ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * ERP_SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 上月期末库存
    */
    @TableField("init_qty")
    private Integer initQty;
    /**
    * 签收上架
    */
    @TableField("in_qty")
    private Integer inQty;
    /**
    * 退货上架
    */
    @TableField("return_qty")
    private Integer returnQty;
    /**
    * 订单签出
    */
    @TableField("out_qty")
    private Integer outQty;
    /**
    * 库存调整
    */
    @TableField("adjust_qty")
    private Integer adjustQty;
    /**
    * 库存调整
    */
    @TableField("check_qty")
    private Integer checkQty;
    /**
    * 其他动账
    */
    @TableField("other_qty")
    private Integer otherQty;
    /**
    * 库存移动
    */
    @TableField("move_qty")
    private Integer moveQty;
    /**
    * 本月期末库存
    */
    @TableField("closing_qty")
    private Integer closingQty;
    /**
    * 即时库存
    */
    @TableField("real_qty")
    private Integer realQty;
    /**
    * 扣减次月数量
    */
    @TableField("next_month_qty")
    private Integer nextMonthQty;
    /**
    * 实际期末库存
    */
    @TableField("estimate_closing_qty")
    private Integer estimateClosingQty;
    /**
    * 差异数量
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String UNIQUE_CODE = "unique_code";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String ACCOUNT_CODE = "account_code";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String BILL_TOPIC = "bill_topic";

    public static final String FLOW_ID = "flow_id";

    public static final String NODE_ID = "node_id";

    public static final String INSTANCE_ID = "instance_id";

    public static final String TASK_ID = "task_id";

    public static final String SOURCE_ID = "source_id";

    public static final String ETL_STATUS = "etl_status";

    public static final String CHECK_KEY = "check_key";

    public static final String CHECK_MONTH = "check_month";

    public static final String SOURCE_SYSTEM_NAME = "source_system_name";

    public static final String PLATFORM_WAREHOUSE_CODE = "platform_warehouse_code";

    public static final String PLATFORM_WAREHOUSE_NAME = "platform_warehouse_name";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String STOCK_SKU = "stock_sku";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String INIT_QTY = "init_qty";

    public static final String IN_QTY = "in_qty";

    public static final String RETURN_QTY = "return_qty";

    public static final String OUT_QTY = "out_qty";

    public static final String ADJUST_QTY = "adjust_qty";

    public static final String CHECK_QTY = "check_qty";

    public static final String OTHER_QTY = "other_qty";

    public static final String MOVE_QTY = "move_qty";

    public static final String CLOSING_QTY = "closing_qty";

    public static final String REAL_QTY = "real_qty";

    public static final String NEXT_MONTH_QTY = "next_month_qty";

    public static final String ESTIMATE_CLOSING_QTY = "estimate_closing_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}