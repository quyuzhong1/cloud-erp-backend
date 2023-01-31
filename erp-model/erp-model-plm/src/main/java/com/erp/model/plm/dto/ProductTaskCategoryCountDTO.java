package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/30 15:58
 */
@Data
@NoArgsConstructor
public class ProductTaskCategoryCountDTO {

    /**
     * 任务类型（1全部任务，2待我完成任务，3待我审核任务），枚举
     */
    private Integer type;

    /**
     * 数量
     */
    private Integer count;
}
