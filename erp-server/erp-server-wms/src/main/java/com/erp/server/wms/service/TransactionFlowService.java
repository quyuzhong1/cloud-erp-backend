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
     * 分页查询即时库存对应的流水
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.TransFlowPagingViewDTO> pagingForInv(PagingDTO<InventoryDTO.TransFlowSearchParamDTO> pagingParamDTO);

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

}
