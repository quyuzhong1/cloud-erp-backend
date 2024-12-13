package com.erp.model.plm.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MouldRefCalcQtyDTO {

    /**
     * 明细id
     */
    private String detailId;

    /**
     * 启用时间
     */
    private LocalDate enableDate;

    /**
     * 返还标准
     */
    private String refundStandard;
}
