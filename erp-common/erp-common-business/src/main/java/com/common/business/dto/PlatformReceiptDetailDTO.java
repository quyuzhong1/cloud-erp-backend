package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *收款单消费DTO
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformReceiptDetailDTO extends UniqueDto {

    /**
     * 编号
     */
    private String code;
    /**
     * 销售订单号
     */
    private String soCode;
    /**
     * 状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 平台删除标识
     */
    private Boolean platformIsDeleted;

    /**
     * 平台明细id
     */
    private String platformDetailId;

    /**
     * 是否作废
     */
    private Boolean isInvalid;
}
