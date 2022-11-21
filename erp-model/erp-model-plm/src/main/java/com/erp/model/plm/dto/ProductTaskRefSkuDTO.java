package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 9:55
 */
@Data
@NoArgsConstructor
public class ProductTaskRefSkuDTO implements Serializable {

    /**
     * 任务id
     */
    private String taskId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * skuid
     */
    private String skuId;

    /**
     * sku名称
     */
    private String skuName;
}
