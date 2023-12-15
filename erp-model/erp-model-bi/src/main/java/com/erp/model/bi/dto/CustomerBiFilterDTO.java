package com.erp.model.bi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BI 筛选条件
 *
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBiFilterDTO extends BiFilterDTO {
    /**
     * 客户属性
     */
    private String customerProperty;
}
