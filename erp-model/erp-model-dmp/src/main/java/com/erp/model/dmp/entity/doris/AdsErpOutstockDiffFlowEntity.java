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
 * 第三方仓出库单据差异表
 * </p>
 *
 * @author shukai
 * @since 2025-11-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ads_erp_outstock_diff_flow")
public class AdsErpOutstockDiffFlowEntity extends BaseEntity<AdsErpOutstockDiffFlowEntity> {

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
    * 出库单号
    */
    @TableField("outstock_code")
    private String outstockCode;
    /**
    * ERP下单单号
    */
    @TableField("so_delivery_code")
    private String soDeliveryCode;
    /**
    * ERP销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 平台原始订单号
    */
    @TableField("platform_order_code")
    private String platformOrderCode;
    /**
    * 出库仓库
    */
    @TableField("platform_warehouse")
    private String platformWarehouse;
    /**
    * 单据日期
    */
    @TableField("platform_bill_date")
    private String platformBillDate;
    /**
    * 单据状态
    */
    @TableField("platform_bill_status")
    private String platformBillStatus;
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
    * 出库数量
    */
    @TableField("outstock_qty")
    private Integer outstockQty;
    /**
    * 流水扣减数量
    */
    @TableField("flow_deduct_qty")
    private Integer flowDeductQty;
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
       * 完成时间
       */
       @TableField("finish_time")
       private String finishTime;


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

    public static final String CHECK_MONTH = "check_month";

    public static final String SOURCE_SYSTEM_NAME = "source_system_name";

    public static final String OUTSTOCK_CODE = "outstock_code";

    public static final String SO_DELIVERY_CODE = "so_delivery_code";

    public static final String SO_CODE = "so_code";

    public static final String PLATFORM_ORDER_CODE = "platform_order_code";

    public static final String PLATFORM_WAREHOUSE = "platform_warehouse";

    public static final String PLATFORM_BILL_DATE = "platform_bill_date";

    public static final String PLATFORM_BILL_STATUS = "platform_bill_status";

    public static final String STOCK_SKU = "stock_sku";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String OUTSTOCK_QTY = "outstock_qty";

    public static final String FLOW_DEDUCT_QTY = "flow_deduct_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}