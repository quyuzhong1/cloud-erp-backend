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
public class AddBoxToSupplyRequest {
    /**
     * 箱子数量
     */
    private Integer amount;
}
