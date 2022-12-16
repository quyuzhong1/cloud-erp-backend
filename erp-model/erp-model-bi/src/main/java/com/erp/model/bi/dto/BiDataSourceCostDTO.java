package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:35
 */
@Data
@NoArgsConstructor
public class BiDataSourceCostDTO {

    /**
     * 月份
     */
    private LocalDateTime month;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 站点
     */
    private String site;

}
