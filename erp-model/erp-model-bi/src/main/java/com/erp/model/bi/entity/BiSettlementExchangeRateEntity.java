package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: 结算汇率表
 * @date 2022/12/19 9:44
 */
@Data
@TableName(value ="bi_settlement_exchange_rate")
public class BiSettlementExchangeRateEntity implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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

}
