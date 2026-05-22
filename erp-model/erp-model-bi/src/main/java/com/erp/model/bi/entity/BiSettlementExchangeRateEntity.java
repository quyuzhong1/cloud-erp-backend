package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: 结算汇率表
 * @date 2022/12/19 9:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "bi_settlement_exchange_rate")
public class BiSettlementExchangeRateEntity extends BaseEntity<BiSettlementExchangeRateEntity> {

    /**
     * 生效日期
     */
    @TableField(value = "settlement_date_begin")
    private LocalDate settlementDateBegin;

    /**
     * 失效日期
     */
    @TableField(value = "settlement_date_end")
    private LocalDate settlementDateEnd;

    /**
     * 汇率
     */
    @TableField(value = "exchange_rate")
    private BigDecimal exchangeRate;

    /**
     * 源币种
     */
    @TableField(value = "source_currency_code")
    private String sourceCurrencyCode;

    /**
     * 目标币种
     */
    @TableField(value = "target_currency_code")
    private String targetCurrencyCode;


    /**
     * 审核状态
     */
    @TableField(value = "approve_status")
    private String approveStatus;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 禁用状态
     */
    @TableField(value = "disabled")
    private Boolean disabled;


    /**
     * 汇率类型
     */
    @TableField(value = "type")
    private String type;

    /**
     * 间接汇率
     */
    @TableField(value = "indirect_exchange_rate")
    private BigDecimal indirectExchangeRate;


    /**
     * 金蝶id
     */
    @TableField(value = "kingdee_id")
    private String kingdeeId;

}
