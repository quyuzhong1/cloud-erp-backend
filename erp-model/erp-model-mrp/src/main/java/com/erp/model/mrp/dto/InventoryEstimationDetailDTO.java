package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InventoryEstimationDetailDTO {

    /**
     * 时间段
     * @see com.erp.model.mrp.enums.TimePeriodEstimateEnum
     */
    private String timePeriodEstimate;

    /**
     * 是否计算真实数据
     */
    private Boolean isReal;

    /**
     * 是否计算模拟
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
