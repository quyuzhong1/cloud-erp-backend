package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;

import java.time.LocalDate;
import java.util.List;

/**
 * @Classname: TransactionFlowService
 * @Description:
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
public interface TransactionFlowService extends SuperService<TransactionFlowEntity> {

    /**
     * 根据单据来源和单据id查询出库存交易流水
     * @param sourceType
     * @param sourceId
     * @return
     */
    List<TransactionFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId);

    /**
     * 修改交易流水为已反审核
     * @param id
     * @param version
     * @return
     */
    int updateUnapprovedById(String id, Integer version);

    /**
     * 记录库存交易流水
     */
    void add(TransactionFlowDTO param, InventoryBusinessTypeEnum businessType,
                                      String transactionRuleId, Integer afterInventoryQty, InventoryModeEnum inventoryModeEnum);

    /**
     * 记录库存交易流水
     * @param param 交易流水
     * @param afterInventoryQty 交易后库存
     */
    void add(TransactionFlowEntity param, Integer afterInventoryQty);


    /**
     * 分页查询即时库存对应的流水
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.TransFlowPagingViewDTO> pagingForInv(PagingDTO<InventoryDTO.TransFlowSearchParamDTO> pagingParamDTO);
    /**
     * 导出即时库存详情Excel
     *
     * @param param
     */
    void exportTransFlow(InventoryDTO.ExportInvFlowSearchParamDTO param);

    /**
     * 分页查询出入库流水
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> paging(PagingDTO<InventoryDTO.InOutStockTransFlowSearchParamDTO> pagingParamDTO);

    /**
     * 导出出入库流水Excel
     *
     * @param param
     */
    void exportExcel(InventoryDTO.ExportInOutStockTransFlowSearchParamDTO param);

    /**
     * 出入库列表分页查询
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> pagingSummary(PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> pagingParamDTO);

    /**
     * 导出出入库列表Excel
     *
     * @param param
     */
    void exportSummaryExcel(InventoryDTO.ExcelInOutStockSummarySearchParamDTO param);

    /**
     * 在途查询分页查询
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryReportDTO.TransportPagingDTO> transportPagingList(PagingDTO<InventoryReportDTO.TransportSearchParamDTO> pagingParamDTO);

    /**
     * 在途查询导出
     *
     * @param pagingParamDTO
     */
    void exportTransportExcel(InventoryReportDTO.ExportTransportSearchParamDTO pagingParamDTO);

    /**
     * 在途查询单据明细分页查询
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryReportDTO.ListTransportPagingDTO> transportList(PagingDTO<InventoryReportDTO.ListTransportSearchParam> pagingParamDTO);

    /**
     * 库存流水重算方法
     *
     * @param startDate      用户输入开始重算时间
     * @param inventoryId    库存id
     * @param  orgName         组织名称
     */
    void overrideInventoryFlow(LocalDate startDate, String inventoryId, String orgName);

    /**
     * @description: 每日库存
     * @author Will
     * @date: 2023/12/6 17:14
     * @param dto
     * @return PagingVO<ListDailyInventoryDTO>
     */
    PagingVO<InventoryReportDTO.ListDailyInventoryDTO> dailyInventoryPaging(PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto);
    /**
     * @param dto
     * @description: 每日库存导出
     * @author Will
     * @date: 2023/12/6 17:14
     */
    void exportDailyInventory(InventoryReportDTO.DailyInventoryParamDTO dto);

    /**
     * 导出
     */
    PagingVO<InventoryReportDTO.ListDailyInventoryDTO> exportInventoryDaily(PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto);
    /**
     * 导出
     */
    PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> exportInventoryInOutStock(PagingDTO<InventoryDTO.ExportInOutStockTransFlowSearchParamDTO> dto);
    /**
     * 导出
     */
    PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> exportInOutStockSummary(PagingDTO<InventoryDTO.ExcelInOutStockSummarySearchParamDTO> dto);
    /**
     * 导出
     */
    PagingVO<InventoryDTO.TransFlowPagingViewDTO> exportInventoryTransFlow(PagingDTO<InventoryDTO.ExportInvFlowSearchParamDTO> dto);

    PagingVO<InventoryReportDTO.TransportPagingDTO> exportInventoryTransport(PagingDTO<InventoryReportDTO.ExportTransportSearchParamDTO> dto);

    /**
     * 通过组织id查询存在流水的库存id
     *
     * @param startDate   开始时间
     * @param orgId       组织id
     * @param inventoryId 库存id
     * @param fromTable 临时表中获取
     * @return List<String>
     */
    List<String> listByOrgId(LocalDate startDate, String orgId, String inventoryId, Boolean fromTable);
}
