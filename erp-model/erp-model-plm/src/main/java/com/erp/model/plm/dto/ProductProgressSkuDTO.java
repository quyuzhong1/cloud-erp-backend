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
public class ProductProgressSkuDTO implements Serializable {

    /**
     * 阶段名
     */
    private String phaseName;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku编码
     */
    private String skuNo;


    /**
     * 任务完成数量
     */
    private Long  finishQty;

    /**
     * 任务总数量
     */
    private Long totalQty;

}
