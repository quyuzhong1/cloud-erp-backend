package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InventoryEstimationDetailDTO {
    /**
     * 建议明细id
     */
    private String detailId;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 是否模拟
     */
    private Boolean isSimulated;
    /**
     * 发货建议
     */
    private List<DeliverySuggestDTO.ListDTO> deliverySuggest;
    /**
     * 采购建议
     */
    private List<PurchaseSuggestDTO.ListDTO> purchaseSuggest;
}
