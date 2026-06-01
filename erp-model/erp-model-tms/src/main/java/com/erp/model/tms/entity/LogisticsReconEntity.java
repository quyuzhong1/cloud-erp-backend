package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * <p>
 * 物流商对账单（主表）
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@TableName("logistics_recon")
public class LogisticsReconEntity extends BaseEntity<LogisticsReconEntity> {

    /**
     * 批次单号，沿用 BusinessNoTypeEnum.CODE_DZ 生成
     */
    @TableField("code")
    private String code;

    /**
     * 对账月份 YYYY-MM
     */
    @TableField("reconciliation_month")
    private String reconciliationMonth;

    /**
     * 业务类型 enum: firstMile头程 / selfDeliver自发货 / lastMile尾程
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 匹配到的导入模板配置 id（cfg_logistics_cost_import.id）
     */
    @TableField("cfg_import_id")
    private String cfgImportId;

    /**
     * 配置类型 enum：logisticsSupplier / platform
     */
    @TableField("cfg_type")
    private String cfgType;

    /**
     * 物流商 id 或平台编码（cfgType=logisticsSupplier 时；对齐 logistics_bill.logistics_supplier_id）
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 物流商/平台名称（冗余展示）
     */
    @TableField("supplier_name")
    private String supplierName;

    /**
     * 处理表格源 sheet 名称
     */
    @TableField("sheet_name")
    private String sheetName;

    /**
     * 上传文件 URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 上传文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 导入失败原因（解析失败、批量入库异常等；导入中失败时记录，状态保持 importing）
     */
    @TableField("import_fail_reason")
    private String importFailReason;

    /**
     * 校验状态（已合并导入状态）importing/pending/confirmed  枚举：LogisticsReconCheckStatusEnum
     */
    @TableField("check_status")
    private String checkStatus;

    /**
     * 导入明细行数（= logistics_recon_detail 条数，导入完成时一次性写入，不随匹配变化）
     */
    @TableField("import_count")
    private Integer importCount;

    /**
     * 导入费用项条数（= logistics_recon_detail_sub 条数，导入完成时一次性写入，仅展示用）
     */
    @TableField("cost_count")
    private Integer costCount;

    /**
     * 对账总金额（logistics_recon_detail_sub.actual_amount 求和）
     * 说明：匹配状态 match_status / 已匹配行数 match_count 不在主表冗余存储，
     * 由列表/详情查询时实时聚合 logistics_recon_detail 派生，避免明细海量数据下每次匹配回写主表。
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 总金额币别（多币别则空）
     */
    @TableField("currency")
    private String currency;

    /**
     * 校验人 id
     */
    @TableField("check_user_id")
    private String checkUserId;

    /**
     * 校验人姓名
     */
    @TableField("check_user_name")
    private String checkUserName;

    /**
     * 校验完成时间（check_status 切到 confirmed 时写入）
     */
    @TableField("check_time")
    private LocalDateTime checkTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    public static final String CODE = "code";
    public static final String RECONCILIATION_MONTH = "reconciliation_month";
    public static final String BUSINESS_TYPE = "business_type";
    public static final String CFG_IMPORT_ID = "cfg_import_id";
    public static final String CFG_TYPE = "cfg_type";
    public static final String SUPPLIER_ID = "supplier_id";
    public static final String SUPPLIER_NAME = "supplier_name";
    public static final String SHEET_NAME = "sheet_name";
    public static final String FILE_URL = "file_url";
    public static final String FILE_NAME = "file_name";
    public static final String IMPORT_FAIL_REASON = "import_fail_reason";
    public static final String CHECK_STATUS = "check_status";
    public static final String IMPORT_COUNT = "import_count";
    public static final String COST_COUNT = "cost_count";
    public static final String TOTAL_AMOUNT = "total_amount";
    public static final String CURRENCY = "currency";
    public static final String CHECK_USER_ID = "check_user_id";
    public static final String CHECK_USER_NAME = "check_user_name";
    public static final String CHECK_TIME = "check_time";
    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
