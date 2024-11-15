package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 10:19
 */
@Data
@NoArgsConstructor
public class ProductProgressShowDTO {

    /**
     * 任务进度集合
     */
    private List<ProductProgressPhaseDTO> phaseList;

    /**
     * 产品sku进度集合
     */
    private List<ProductProgressSkuDTO> skuList;
}
