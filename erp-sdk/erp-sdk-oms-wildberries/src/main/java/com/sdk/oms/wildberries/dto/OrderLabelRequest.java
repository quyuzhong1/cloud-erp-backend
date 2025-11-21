package com.sdk.oms.wildberries.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
@Builder
public class OrderLabelRequest {
    /**
     * 供货单名称 取erp销售订单单号
     */
    private List<Long> orders;
    //58
    private Integer width=58;
    private Integer height=40;
    //"svg" "zplv" "zplh" "png"
    private String type="png";
}
