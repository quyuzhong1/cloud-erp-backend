package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *收款单消费DTO
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformReceiptDTO extends UniqueDto {

    /**
     * 单据编码
     */
    private String code;
    /**
     * 收款金额
     */
    private BigDecimal amount;
    /**
     * 币种
     */
    private String currency;
    /**
     * 订单状态
     */
    private String status;
    /**
     * 客户编号
     */
    private String customerCode;
    /**
     * 是否入账
     */
    private Boolean isPosted;

    /**
     * 入账账户Id
     */
    private String postedAccountId;

    /**
     * 收款日期
     */
    private LocalDate receiptDate;
    /**
     * 收款方式
     */
    private String receiptMethod;
    /**
     * 收款账号
     */
    private String receiptAccount;

    private String remark;
    /**
     * 平台订单id
     */
    private String platformId;
    /**
     * 第三方系统
     */
    private String thirdSystem;

    private List<PlatformReceiptDetailDTO> detailList;
}
