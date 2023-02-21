package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 11:01
 */
@Data
@NoArgsConstructor
public class ProductPlanStatisticsDTO implements Serializable {

    /**
     * 总数量
     */
    private Long totalCount;

    /**
     * 总数量描述
     */
    private String describe;

    /**
     * 本月数量
     */
    private Long thisMonthCount;

    /**
     * 本月数量描述
     */
    private String thisDescribe;


}
