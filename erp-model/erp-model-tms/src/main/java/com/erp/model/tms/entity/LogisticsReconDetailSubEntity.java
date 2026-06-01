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


/**
 * <p>
 * 物流商对账费用项明细（费用项级，对应 tms_cost_detail）
 * 一条 detail 1:N 多条费用项；匹配粒度为费用项，本表持有匹配状态和失败原因。
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@TableName("logistics_recon_detail_sub")
public class LogisticsReconDetailSubEntity extends BaseEntity<LogisticsReconDetailSubEntity> {

    /**
     * 对账单 id（→ logistics_recon.id，冗余加速跨表查询）
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 所属对账明细 id（→ logistics_recon_detail.id）
     */
    @TableField("detail_id")
    private String detailId;

    /**
     * 同 detail 下费用项顺序（横向列展开按列序，纵向按 Excel 行内顺序）
     */
    @TableField("seq_no")
    private Integer seqNo;

    /**
     * 导入识别费用名称（配置字段 costItem 取值，或横向模式下的列标题）
     */
    @TableField("cost_name")
    private String costName;

    /**
     * 映射后费用项配置 id（cfg_cost.id；导入时如能映射则填，否则待补齐）
     */
    @TableField("cfg_cost_id")
    private String cfgCostId;

    /**
     * 映射后费用名称（冗余展示）
     */
    @TableField("cfg_cost_name")
    private String cfgCostName;

    /**
     * 实际费用金额（配置字段 actualAmount，按 ETL 规则做过正负转换；金额按"原币"记）
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 预估费用金额（配置字段 estimatedAmount；同样以原币计）
     */
    @TableField("estimated_amount")
    private BigDecimal estimatedAmount;

    /**
     * 原币别（默认继承 detail.currency；保留以支持单费用项独立币别）
     */
    @TableField("currency")
    private String currency;

    /**
     * 结算币别（沿用 shop_info.settlement_currency 命名，按物流商/平台/公司维度取）
     */
    @TableField("settlement_currency")
    private String settlementCurrency;

    /**
     * 结算汇率（原币 → 结算币，按对账月份 bi_settlement_exchange_rate 查得）
     */
    @TableField("settlement_exchange_rate")
    private BigDecimal settlementExchangeRate;

    /**
     * 本位币币别（沿用 ERP *_local_currency 命名，通常为公司本位币如 CNY）
     */
    @TableField("local_currency")
    private String localCurrency;

    /**
     * 本位币汇率（原币 → 本位币，按对账月份取）
     */
    @TableField("local_exchange_rate")
    private BigDecimal localExchangeRate;

    /**
     * 费用项匹配状态  枚举：LogisticsReconDetailMatchStatusEnum
     * unmatched / matching / matched / failed
     */
    @TableField("match_status")
    private String matchStatus;

    /**
     * 匹配失败原因（如运单匹配不上物流单、费用项映射失败等）
     */
    @TableField("match_fail_reason")
    private String matchFailReason;

    public static final String MAIN_ID = "main_id";
    public static final String DETAIL_ID = "detail_id";
    public static final String SEQ_NO = "seq_no";
    public static final String COST_NAME = "cost_name";
    public static final String CFG_COST_ID = "cfg_cost_id";
    public static final String CFG_COST_NAME = "cfg_cost_name";
    public static final String ACTUAL_AMOUNT = "actual_amount";
    public static final String ESTIMATED_AMOUNT = "estimated_amount";
    public static final String CURRENCY = "currency";
    public static final String SETTLEMENT_CURRENCY = "settlement_currency";
    public static final String SETTLEMENT_EXCHANGE_RATE = "settlement_exchange_rate";
    public static final String LOCAL_CURRENCY = "local_currency";
    public static final String LOCAL_EXCHANGE_RATE = "local_exchange_rate";
    public static final String MATCH_STATUS = "match_status";
    public static final String MATCH_FAIL_REASON = "match_fail_reason";

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
