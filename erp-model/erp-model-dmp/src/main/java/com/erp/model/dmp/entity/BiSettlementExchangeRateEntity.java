package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
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
    @TableField("id")
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
     * 结算日期
     */
    @TableField(value = "settlement_date")
    private LocalDateTime settlementDate;

    /**
     * 汇率
     */
    @TableField(value = "rate")
    private BigDecimal rate;

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
