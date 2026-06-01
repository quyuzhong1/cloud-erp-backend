package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 物流商对账单 - 关联关系表
 * 对账费用项 (logistics_recon_detail_sub) ↔ ERP 物流单/费用/费用项 的桥梁
 * 写入/逻辑删除以 detail 为单位整批操作（匹配粒度对齐 detail），
 * 表行粒度对齐 tms_cost_detail（1:1，由唯一索引 uniq_detail_sub_active 兜底）。
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@TableName("logistics_recon_ref_logistics_bill")
public class LogisticsReconRefLogisticsBillEntity extends BaseEntity<LogisticsReconRefLogisticsBillEntity> {

    /**
     * 对账单 id（冗余，按对账单批量查询/解绑）
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 对账明细 id（冗余，按行筛选；行级 FK = 同行所有费用项共用同一 logistics_bill_id）
     */
    @TableField("detail_id")
    private String detailId;

    /**
     * 对账费用项 id（→ logistics_recon_detail_sub.id，核心粒度键）
     */
    @TableField("detail_sub_id")
    private String detailSubId;

    /**
     * 写入的 logistics_bill.id（行级）
     */
    @TableField("logistics_bill_id")
    private String logisticsBillId;

    /**
     * 写入的 logistics_bill_detail.id（行级）
     */
    @TableField("logistics_bill_detail_id")
    private String logisticsBillDetailId;

    /**
     * 写入的 logistics_bill_cost.id（行级，= tms_cost_detail.main_id）
     */
    @TableField("logistics_bill_cost_id")
    private String logisticsBillCostId;

    /**
     * 写入的 tms_cost_detail.id（费用项级，1:1）
     */
    @TableField("tms_cost_detail_id")
    private String tmsCostDetailId;

    /**
     * 匹配类型 enum：auto / manual / newBill  枚举：LogisticsReconRefMatchTypeEnum
     */
    @TableField("match_type")
    private String matchType;

    /**
     * 匹配人 id
     */
    @TableField("match_user_id")
    private String matchUserId;

    /**
     * 匹配人姓名
     */
    @TableField("match_user_name")
    private String matchUserName;

    /**
     * 匹配时间
     */
    @TableField("match_time")
    private LocalDateTime matchTime;

    /**
     * 合并匹配时使用的导入处理：importUpdate / importAddOld / importAddNew
     */
    @TableField("import_type")
    private String importType;

    public static final String MAIN_ID = "main_id";
    public static final String DETAIL_ID = "detail_id";
    public static final String DETAIL_SUB_ID = "detail_sub_id";
    public static final String LOGISTICS_BILL_ID = "logistics_bill_id";
    public static final String LOGISTICS_BILL_DETAIL_ID = "logistics_bill_detail_id";
    public static final String LOGISTICS_BILL_COST_ID = "logistics_bill_cost_id";
    public static final String TMS_COST_DETAIL_ID = "tms_cost_detail_id";
    public static final String MATCH_TYPE = "match_type";
    public static final String MATCH_USER_ID = "match_user_id";
    public static final String MATCH_USER_NAME = "match_user_name";
    public static final String MATCH_TIME = "match_time";
    public static final String IMPORT_TYPE = "import_type";

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
