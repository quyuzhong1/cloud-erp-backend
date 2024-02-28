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
     * 任务类型（all全部，waitMe待我审核,approve已审核,reject不通过），枚举
     */
    private String type;
    /**
     * 任务名称
     */
    private String name;

    /**
     * 数量
     */
    private Integer count;
}
