package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 产品任务进度DTO
 * @date 2022/11/21 9:13
 */

@Data
@NoArgsConstructor
public class ProductSkuProgressDTO implements Serializable {

    /**
     * sku名
     */
    private String skuName;

    /**
     * 任务完成数量
     */
    private Long  finishQty;

    /**
     * 任务总数量
     */
    private Long totalQty;

}
