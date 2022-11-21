package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 产品阶段进度
 * @date 2022/11/21 9:23
 */
@Data
@NoArgsConstructor
public class ProductPhaseProgressDTO implements Serializable {

    /**
     * 阶段名
     */
    private String phaseName;

    /**
     * 任务完成数量
     */
    private Long  finishQty;

    /**
     * 任务总数量
     */
    private Long totalQty;

    /**
     * 产品sku进度集合
     */
    private List<ProductSkuProgressDTO> skuList;
}
