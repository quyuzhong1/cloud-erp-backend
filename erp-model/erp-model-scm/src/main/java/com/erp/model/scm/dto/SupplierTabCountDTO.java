package com.erp.model.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zdy
 * @version 1.0

 * @date 2023/1/30 15:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierTabCountDTO {

    /**
     * 任务类型（0全部，1待我审核,2已审核,3不通过），枚举
     */
    private Integer type;
    /**
     * 任务名称
     */
    private String name;

    /**
     * 数量
     */
    private Integer count;
}
