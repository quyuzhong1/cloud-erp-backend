package com.erp.model.plm.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MouldRefCalcQtyDTO {

    private String id;
    /**
     * 产品经理
     */
    private String productManagerId;
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
    /**
     * 模具编号
     */
    private String mouldNo;
    /**
     * 供应商id
     */
    private String supplierId;
    /**
     * 名字
     */
    private String name;

}
