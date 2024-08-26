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

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * @param param
     * @param response
     */
    void exportTransFlow(InventoryDTO.ExportInvFlowSearchParamDTO param, HttpServletResponse response);

    /**
     * 分页查询出入库流水
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> paging(PagingDTO<InventoryDTO.InOutStockTransFlowSearchParamDTO> pagingParamDTO);

    /**
     * 导出出入库流水Excel
     * @param param
     * @param response
     */
    void exportExcel(InventoryDTO.ExportInOutStockTransFlowSearchParamDTO param, HttpServletResponse response);

    /**
     * 出入库列表分页查询
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> pagingSummary(PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> pagingParamDTO);

    /**
     * 导出出入库列表Excel
     * @param param
     * @param response
     */
    void exportSummaryExcel(InventoryDTO.ExcelInOutStockSummarySearchParamDTO param, HttpServletResponse response);

    /**
     * 在途查询分页查询
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryReportDTO.TransportPagingDTO> transportPagingList(PagingDTO<InventoryReportDTO.TransportSearchParamDTO> pagingParamDTO);

    /**
     * 在途查询导出
     * @param pagingParamDTO
     * @param response
     */
    void exportTransportExcel(InventoryReportDTO.ExportTransportSearchParamDTO pagingParamDTO, HttpServletResponse response);

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
     * @description: 每日库存导出
     * @author Will
     * @date: 2023/12/6 17:14
     * @param dto
     * @param response
     */
    void exportDailyInventory(InventoryReportDTO.DailyInventoryParamDTO dto, HttpServletResponse response);

    /**
     * 通过组织id查询存在流水的库存id
     * @param startDate 开始时间
     * @param orgId 组织id
     * @param inventoryId 库存id
     * @return  List<String>
     */
    List<String> listByOrgId(LocalDate startDate, String orgId, String inventoryId);
}
