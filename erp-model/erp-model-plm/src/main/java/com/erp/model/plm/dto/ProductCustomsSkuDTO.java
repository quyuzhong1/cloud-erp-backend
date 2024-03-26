package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author zdy
 * @ClassName ProductCustomsSkuDTO
 * @description: sku自定义映射表
 * @date 2024年03月26日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCustomsSkuDTO {
    @NotEmpty(message = "skuIds不能为空")
    private List<String> skuIds;

    private String country;
}
