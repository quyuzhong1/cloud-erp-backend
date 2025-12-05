package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
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
 * ERP退货入库单差异表
 * </p>
 *
 * @author shukai
 * @since 2025-11-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ads_erp_diff_return_instock_sync")
public class AdsErpDiffReturnInstockSyncEntity extends BaseEntity<AdsErpDiffReturnInstockSyncEntity> {

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
    * 核对周期页面查询
    */
    @TableField("check_month_query")
    private String checkMonthQuery;
    /**
    * 核对仓库
    */
    @TableField("source_system_name")
    private String sourceSystemName;
    /**
    * 平台单据名称
    */
    @TableField("platform_bill_name")
    private String platformBillName;
    /**
    * 平台单据状态名称
    */
    @TableField("platform_bill_status_name")
    private String platformBillStatusName;
    /**
    * 平台ERP店铺
    */
    @TableField("platform_shop_name")
    private String platformShopName;
    /**
    * 平台平台名称
    */
    @TableField("platform_sales_platform_name")
    private String platformSalesPlatformName;
    /**
    * 平台原始单号
    */
    @TableField("platform_order_code")
    private String platformOrderCode;
    /**
    * 平台销售单号
    */
    @TableField("platform_so_code")
    private String platformSoCode;
    /**
    * 平台入库单号
    */
    @TableField("platform_return_instock_code")
    private String platformReturnInstockCode;
    /**
    * 平台库存SKU
    */
    @TableField("stock_sku")
    private String stockSku;
    /**
    * 平台库存SKU数量
    */
    @TableField("stock_qty")
    private Integer stockQty;
    /**
    * 平台库存SKU*数量
    */
    @TableField("stock_sku_qty")
    private String stockSkuQty;
    /**
    * 平台ERP_SKU
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 平台ERP_SKU数量
    */
    @TableField("platform_qty")
    private Integer platformQty;
    /**
    * 平台ERP_SKU*数量
    */
    @TableField("platform_sku_qty")
    private String platformSkuQty;
    /**
    * 平台仓库名称
    */
    @TableField("platform_warehouse_name")
    private String platformWarehouseName;
    /**
    * 平台ERP仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 第三方单据日期
    */
    @TableField("platform_bill_date")
    private Date platformBillDate;
    /**
    * ERP单据名称
    */
    @TableField("bill_name")
    private String billName;
    /**
    * ERP单据状态名称
    */
    @TableField("bill_status_name")
    private String billStatusName;
    /**
    * ERP店铺
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * ERP平台
    */
    @TableField("sales_platform_name")
    private String salesPlatformName;
    /**
    * ERP原始单号
    */
    @TableField("order_code")
    private String orderCode;
    /**
    * ERP销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * ERP入库单号
    */
    @TableField("return_instock_code")
    private String returnInstockCode;
    /**
    * ERP_SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * ERP_SKU数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * ERP_SKU*数量
    */
    @TableField("sku_qty")
    private String skuQty;
    /**
    * ERP仓库名称
    */
    @TableField("erp_warehouse_name")
    private String erpWarehouseName;
    /**
    * erp单据日期
    */
    @TableField("bill_date")
    private Date billDate;
    /**
    * 差异数量
    */
    @TableField("diff_qty")
    private String diffQty;
    /**
    * 差异标签
    */
    @TableField("diff_tag")
    private String diffTag;
    /**
    * 差异标签名称
    */
    @TableField("diff_tag_name")
    private String diffTagName;
    /**
    * 差异详情
    */
    @TableField("diff_desc")
    private String diffDesc;
    /**
    * 建议处理方式
    */
    @TableField("suggest_type")
    private String suggestType;
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
    * 执行完成时间
    */
    @TableField("finish_time")
    private Date finishTime;


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

    public static final String CHECK_MONTH_QUERY = "check_month_query";

    public static final String SOURCE_SYSTEM_NAME = "source_system_name";

    public static final String PLATFORM_BILL_NAME = "platform_bill_name";

    public static final String PLATFORM_BILL_STATUS_NAME = "platform_bill_status_name";

    public static final String PLATFORM_SHOP_NAME = "platform_shop_name";

    public static final String PLATFORM_SALES_PLATFORM_NAME = "platform_sales_platform_name";

    public static final String PLATFORM_ORDER_CODE = "platform_order_code";

    public static final String PLATFORM_SO_CODE = "platform_so_code";

    public static final String PLATFORM_RETURN_INSTOCK_CODE = "platform_return_instock_code";

    public static final String STOCK_SKU = "stock_sku";

    public static final String STOCK_QTY = "stock_qty";

    public static final String STOCK_SKU_QTY = "stock_sku_qty";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_QTY = "platform_qty";

    public static final String PLATFORM_SKU_QTY = "platform_sku_qty";

    public static final String PLATFORM_WAREHOUSE_NAME = "platform_warehouse_name";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String PLATFORM_BILL_DATE = "platform_bill_date";

    public static final String BILL_NAME = "bill_name";

    public static final String BILL_STATUS_NAME = "bill_status_name";

    public static final String SHOP_NAME = "shop_name";

    public static final String SALES_PLATFORM_NAME = "sales_platform_name";

    public static final String ORDER_CODE = "order_code";

    public static final String SO_CODE = "so_code";

    public static final String RETURN_INSTOCK_CODE = "return_instock_code";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String SKU_QTY = "sku_qty";

    public static final String ERP_WAREHOUSE_NAME = "erp_warehouse_name";

    public static final String BILL_DATE = "bill_date";

    public static final String DIFF_QTY = "diff_qty";

    public static final String DIFF_TAG = "diff_tag";

    public static final String DIFF_TAG_NAME = "diff_tag_name";

    public static final String DIFF_DESC = "diff_desc";

    public static final String SUGGEST_TYPE = "suggest_type";

    public static final String REMARK = "remark";

    public static final String EXEC_STATUS = "exec_status";

    public static final String EXEC_STATUS_NAME = "exec_status_name";

    public static final String FINISH_TIME = "finish_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}