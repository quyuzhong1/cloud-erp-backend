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
public class CreateWarehouseRequest {
    private String name;
    private Integer officeId;
}
