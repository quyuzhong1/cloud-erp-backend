package com.erp.server.wms.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.TransactionFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 库存报表管理
 * @Classname: InventoryController

 * @CreateTime: 2023-05-08  18:52
 * @Author: zhangchunlin
 */
@RestController
@LogSystemModule("即时库存")
@RequestMapping(value = "/inventory")
public class InventoryController extends BaseController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private TransactionFlowService transactionFlowService;

    /**
     * 即时库存分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<InventoryDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<InventoryDTO.SearchParamDTO> dto) {
        return success(inventoryService.paging(dto));
    }

    /**
     * 即时库存导出
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "即时库存导出")
    @PostMapping(value = "/exportInventoryExcel")
    public ApiResult<Void> exportInventoryExcel(@RequestBody InventoryDTO.ExportSearchParamDTO dto, HttpServletResponse response) {
        inventoryService.exportExcel(dto, response);
        return null;
    }

    /**
     * 即时库存查看流水
     * @param dto
     * @return
     */
    @PostMapping("/pageTransFlow")
    public ApiResult<PagingVO<InventoryDTO.TransFlowPagingViewDTO>> pageTransFlow(@RequestBody @Validated PagingDTO<InventoryDTO.TransFlowSearchParamDTO> dto) {
        return success(transactionFlowService.pagingForInv(dto));
    }

    /**
     * 即时库存详情导出
     * @param dto
     * @return
     */
    @PostMapping("/exportTransFlow")
    public void exportTransFlow(@RequestBody InventoryDTO.ExportInvFlowSearchParamDTO dto, HttpServletResponse response) {
        transactionFlowService.exportTransFlow(dto,response);
    }


    /**
     * 出入库流水分页列表
     * @param dto
     * @return
     */
    @PostMapping("/pageInOutStock")
    public ApiResult<PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO>> pageInOutStock(@RequestBody @Validated PagingDTO<InventoryDTO.InOutStockTransFlowSearchParamDTO> dto) {
        return success(transactionFlowService.paging(dto));
    }

    /**
     * 出入库流水导出
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "出入库流水导出")
    @PostMapping(value = "/exportExcelInOutStock")
    public ApiResult<Void> exportExcelInOutStock(@RequestBody InventoryDTO.ExportInOutStockTransFlowSearchParamDTO dto, HttpServletResponse response) {
        transactionFlowService.exportExcel(dto, response);
        return null;
    }

    /**
     * 出入库列表分页列表
     * @param dto
     * @return
     */
    @PostMapping("/pageInOutStockSummary")
    public ApiResult<PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO>> pageInOutStockSummary(@RequestBody @Validated PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> dto) {
        return success(transactionFlowService.pagingSummary(dto));
    }

    /**
     * 出入库列表导出
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "出入库列表导出")
    @PostMapping(value = "/exportExcelInOutStockSummary")
    public void exportExcelInOutStockSummary(@RequestBody InventoryDTO.ExcelInOutStockSummarySearchParamDTO dto, HttpServletResponse response) {
        transactionFlowService.exportSummaryExcel(dto, response);
    }

    /**
     * 查询可用库存，特别注意：不传仓位字段则查询仓库下面的该SKU的可用库存，传仓位（含空字符串）则查询仓库下面该仓位的可用库存
     * @author Will
     * @date: 2023/5/11 10:10
     * @param dto
     * @return ApiResult<Integer>
     */
    @PostMapping(value = "/getUsableInventoryTotal")
    public ApiResult<Integer> getUsableInventoryTotal(@RequestBody @Validated InventoryDTO.UsableInventoryParamDTO dto) {
        Integer usableInventoryTotal = inventoryService.getUsableInventoryTotal(dto.getWarehouseId(), dto.getSkuId(), dto.getWarehouseLocation());
        return success(usableInventoryTotal);
    }


    /**
     * 查询状态库存，特别注意：不传仓位字段则查询仓库下面的该SKU的状态库存，传仓位（含空字符串）则查询仓库下面该仓位的可用库存
     * @author Will
     * @date: 2023/5/11 10:10
     * @param dto
     * @return ApiResult<Integer>
     */
    @PostMapping(value = "/getInventoryQty")
    public ApiResult<InventoryDTO.InventoryQtyDTO> getInventoryQty(@RequestBody @Validated InventoryDTO.InventoryBySkuNoDTO dto) {
        InventoryDTO.InventoryQtyDTO  result= inventoryService.getInventoryQty(dto);
        return success(result);
    }

    /**
     * 在途库存分页列表
     * @param dto
     * @return
     */
    @PostMapping("/transport/paging")
    public ApiResult<PagingVO<InventoryReportDTO.TransportPagingDTO>> transportPaging(@RequestBody @Validated PagingDTO<InventoryReportDTO.TransportSearchParamDTO> dto) {
        return success(transactionFlowService.transportPagingList(dto));
    }

    /**
     * 在途库存导出
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "在途库存导出")
    @PostMapping(value = "/exportTransport")
    public void exportTransport(@RequestBody InventoryReportDTO.ExportTransportSearchParamDTO dto, HttpServletResponse response) {
        transactionFlowService.exportTransportExcel(dto, response);
    }

    /**
     * 在途库存单据明细分页列表
     * @param dto
     * @return
     */
    @PostMapping("/transport/list")
    public ApiResult<PagingVO<InventoryReportDTO.ListTransportPagingDTO>> transportList(@RequestBody @Validated PagingDTO<InventoryReportDTO.ListTransportSearchParam> dto) {
        return success(transactionFlowService.transportList(dto));
    }

    /**
     * 库龄计算表分页列表
     * @param dto
     * @return
     */
    @PostMapping("/inventoryAge/paging")
    public ApiResult<PagingVO<LinkedHashMap>> inventoryAgePaging(@RequestBody @Validated PagingDTO<InventoryReportDTO.InventoryAgeSearchParamDTO> dto) {
        return success(inventoryService.inventoryAgePaging(dto));
    }

    /**
     * 库龄计算表导出
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "库龄计算表导出")
    @PostMapping(value = "/exportInventoryAge")
    public void exportInventoryAge(@RequestBody InventoryReportDTO.ExportInventoryAgeSearchParamDTO dto, HttpServletResponse response) {
        inventoryService.exportInventoryAge(dto, response);
    }

    /**
     * 根据条件查询库存信息
     * @Author Luo_WG
     * @Date 2023/11/1 19:27
     * @param dto
     * @return java.util.List<com.erp.model.wms.entity.InventoryEntity>
     **/
    @PostMapping(value = "/listByParam")
    public ApiResult<List<InventoryDTO.UsableInventoryViewDTO>> listByParam(@RequestBody ValidList<InventoryDTO.UsableInventoryParamDTO> dto) {
        List<InventoryDTO.UsableInventoryViewDTO> list = inventoryService.listByParam(dto.getList());
        return success(list);
    }


    /**
     * 每日库存列表
     * @param dto
     * @return
     */
    @PostMapping("/dailyInventoryPaging")
    public ApiResult<PagingVO<InventoryReportDTO.ListDailyInventoryDTO>> dailyInventoryPaging(@RequestBody @Validated PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto) {
        return success(transactionFlowService.dailyInventoryPaging(dto));
    }

    /**
     * 每日库存导出
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "每日库存导出")
    @PostMapping(value = "/exportDailyInventory")
    public void exportDailyInventory(@RequestBody InventoryReportDTO.DailyInventoryParamDTO dto, HttpServletResponse response) {
        transactionFlowService.exportDailyInventory(dto, response);
    }
}