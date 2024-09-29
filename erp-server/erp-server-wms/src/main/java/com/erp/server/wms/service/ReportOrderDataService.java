package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.entity.ReportOrderDataEntity;

import java.util.List;

/**
 * <p>
 * 订单报表信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-24
 */
public interface ReportOrderDataService extends SuperService<ReportOrderDataEntity> {

    /**
    * 新增修改
    * @author will
    * @date: 2024-09-24
    * @param list
    * @return
    */
    Boolean batchAddOrUpdate(List<ReportOrderDataDTO.UpdateDTO> list);

    /**
     * 生成虚拟仓报表
     * @author will
     * @date 2024/9/27 9:29
     */
    void generateVirtualReport();

    /**
     * 生成报表数据
     * @author will
     * @date 2024/9/26 14:18
     */
    void generateReportOrderData();

    /**
     * 生成订单需求明细数据
     * @author will
     * @date 2024/9/26 19:18
     */
    void generateReportOrderDemandDetail(List<ReportOrderDataEntity> reportOrderDataList, List<BomChildrenSkuDTO> bomChildrenSkuList, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList, Boolean isSplit);
    /**
     * 生成缺货数据
     * @author will
     * @date 2024/9/26 19:18
     */
    void generateReportOrderDemand(List<BomChildrenSkuDTO> bomChildrenSkuList, Boolean isSplit);
    /**
     * 生成销售看板数据
     * @author will
     * @date 2024/9/26 19:20
     */
    void generateReportOrderSales(List<ReportOrderDataEntity> reportOrderDataList, List<BomChildrenSkuDTO> bomChildrenSkuList, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList, Boolean isSplit, CfgSettingVirtualValueDTO.SalesDashboardDTO salesDashboardDTO);
}
