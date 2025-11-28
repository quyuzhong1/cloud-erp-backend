package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 平台库存差异
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ads_erp_inventory_diff")
public class AdsErpInventoryDiffEntity extends BaseEntity<AdsErpInventoryDiffEntity> {

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
     * 核对周期页面查询
     */
    @TableField("check_month_query")
    private String checkMonthQuery;
    /**
    * 来源仓库ID
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 来源仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
    * 来源系统仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 来源系统SKU_ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 来源系统SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 来源系统产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 本月期初库存
    */
    @TableField("init_qty")
    private Integer initQty;
    /**
    * 本期出库数量
    */
    @TableField("outstock_qty")
    private Integer outstockQty;
    /**
    * 本期入库数量
    */
    @TableField("instock_qty")
    private Integer instockQty;
    /**
    * 本月期末库存（计算值：期初+入库-出库）
    */
    @TableField("closing_qty")
    private Integer closingQty;
    /**
    * 本月期末库存（每日来源数据）按照仓库核对月份月底的结余库存数量
    */
    @TableField("estimated_closing_qty")
    private Integer estimatedClosingQty;
    /**
    * 平台在途期末库存-本月期末
    */
    @TableField("platform_closing_intransit_qty")
    private Integer platformClosingIntransitQty;
    /**
    * (目的仓)平台在库-本月期末(平台流水差异-本月期末(调整))
    */
    @TableField("platform_closing_qty")
    private Integer platformClosingQty;
    /**
    * (目的仓)平台仓期末库存合计（含在途）
    */
    @TableField("platform_closing_total_qty")
    private Integer platformClosingTotalQty;
    /**
    * 总差异数量（ERP期末-平台期末）/平台在库库存+平台在途-本月期末
    */
    @TableField("diff_closing_total_qty")
    private Integer diffClosingTotalQty;
    /**
    * 期末差异数量
    */
    @TableField("diff_closing_qty")
    private Integer diffClosingQty;
    /**
    * 在途差异数量
    */
    @TableField("diff_closing_intransit_qty")
    private Integer diffClosingIntransitQty;
    /**
    * 差异说明备注
    */
    @TableField("remark")
    private String remark;
    /**
     * 执行状态
     */
    @TableField("exec_status")
    private String execStatus;
    /**
     * 执行状态名称
     */
    @TableField("exec_status_name")
    private String execStatusName;
    /**
     * 执行完成时间
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;



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

    public static final String INIT_QTY = "init_qty";

    public static final String OUTSTOCK_QTY = "outstock_qty";

    public static final String INSTOCK_QTY = "instock_qty";

    public static final String CLOSING_QTY = "closing_qty";

    public static final String ESTIMATED_CLOSING_QTY = "estimated_closing_qty";

    public static final String PLATFORM_CLOSING_INTRANSIT_QTY = "platform_closing_intransit_qty";

    public static final String PLATFORM_CLOSING_QTY = "platform_closing_qty";

    public static final String PLATFORM_CLOSING_TOTAL_QTY = "platform_closing_total_qty";

    public static final String DIFF_CLOSING_TOTAL_QTY = "diff_closing_total_qty";

    public static final String DIFF_CLOSING_QTY = "diff_closing_qty";

    public static final String DIFF_CLOSING_INTRANSIT_QTY = "diff_closing_intransit_qty";

    public static final String REMARK = "remark";

    public static final String FINISH_TIME = "finish_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}