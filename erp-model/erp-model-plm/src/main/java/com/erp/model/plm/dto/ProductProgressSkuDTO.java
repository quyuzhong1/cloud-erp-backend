package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 产品任务进度DTO
 * @date 2022/11/21 9:13
 */

@Data
@NoArgsConstructor
public class ProductProgressSkuDTO implements Serializable {


    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * sku进度集合
     */
    private List<ProductProgressPhaseDTO> skuPhaseList;


}
