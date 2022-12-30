package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 17:28
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringViewVO {

    /**
     * 销量监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> salesQtyMonitoring;

    /**
     * 销售额监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> salesAmountMonitoring;

    /**
     * 新品销售额监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> newProductsMonitoring;

    /**
     * 老品销售额监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> oldProductsMonitoring;

    /**
     * 品牌销售额监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> brandNameMonitoring;

    /**
     * 品类销售额监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> categoryMonitoring;

    /**
     * 人员销售额监控
     */
    private SeriesVO<BiSalesMonitoringTableVO> chargeNameMonitoring;
}
