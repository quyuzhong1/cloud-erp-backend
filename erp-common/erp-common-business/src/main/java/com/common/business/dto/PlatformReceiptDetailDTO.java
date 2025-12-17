package com.common.business.dto;

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
     * 平台明细id
     */
    private String platformDetailId;

    /**
     * 是否作废
     */
    private Boolean isInvalid;

    /**
     * 是否作废
     */
    private Boolean platformIsDeleted;

    /**
     * erp销售订单Id
     */
    private String erpSoId;

    /**
     * 附件列表
     */
    private List<AttachDTO> attachmentList;
}
