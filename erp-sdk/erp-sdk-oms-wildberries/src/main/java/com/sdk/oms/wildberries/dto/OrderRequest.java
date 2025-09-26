package com.sdk.oms.wildberries.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
@Builder
public class OrderRequest {
    private Integer limit;
    private Integer next;
    //时间戳
    private Long dateFrom;
    private Long dateTo;
}
