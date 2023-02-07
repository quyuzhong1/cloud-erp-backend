package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/7 10:04
 */
@Data
@NoArgsConstructor
public class DeliveryDocsGroupDTO {

    /**
     * 交付文档名
     */
    private String deliveryDocsName;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务名
     */
    private String taskName;

    /**
     * 文档信息
     */
    List<DeliveryDocsDTO>  docsList;
}
