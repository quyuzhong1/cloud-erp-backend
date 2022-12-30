package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:35
 */
@Data
@NoArgsConstructor
public class BiDataSourceCostSearchDTO {

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 月份
     */
    private String month;
    
}
