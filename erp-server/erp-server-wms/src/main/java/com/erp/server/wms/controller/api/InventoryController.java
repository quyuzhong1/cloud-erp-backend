package com.erp.server.wms.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.TransactionFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 库存报表管理
 * @Classname: InventoryController
 * @Description: TODO
 * @CreateTime: 2023-05-08  18:52
 * @Author: zhangchunlin
 */
@RestController
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
    @PostMapping(value = "/exportExcel")
    public ApiResult<Void> exportExcel(@RequestBody InventoryDTO.ExportSearchParamDTO dto, HttpServletResponse response) {
        inventoryService.exportExcel(dto, response);
        return success();
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
     * 出入库流水分页列表
     * @param dto
     * @return
     */
    @PostMapping("/pageInOutStock")
    public ApiResult<PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO>> pageInOutStock(@RequestBody @Validated PagingDTO<InventoryDTO.InOutStockTransFlowSearchParamDTO> dto) {
        return success(null);
    }

    /**
     * 出入库流水导出
     * @param dto
     * @param response
     * @return
     */
    @PostMapping(value = "/exportExcelInOutStock")
    public ApiResult<Void> exportExcelInOutStock(@RequestBody InventoryDTO.ExportInOutStockTransFlowSearchParamDTO dto, HttpServletResponse response) {
        return success();
    }

    /**
     * 出入库列表分页列表
     * @param dto
     * @return
     */
    @PostMapping("/pageInOutStockSummary")
    public ApiResult<PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO>> pageInOutStockSummary(@RequestBody @Validated PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> dto) {
        return success(null);
    }

    /**
     * 出入库列表导出
     * @param dto
     * @param response
     * @return
     */
    @PostMapping(value = "/exportExcelInOutStockSummary")
    public ApiResult<Void> exportExcelInOutStockSummary(@RequestBody InventoryDTO.InOutStockSummarySearchParamDTO dto, HttpServletResponse response) {
        return success();
    }

    /**
     * 查询可用库存
     * @author Will
     * @date: 2023/5/11 10:10
     * @param dto
     * @return ApiResult<Integer>
     */
    @PostMapping(value = "/getUsableInventoryTotal")
    public ApiResult<Integer> getUsableInventoryTotal(@RequestBody @Validated InventoryDTO.UsableInventoryParamDTO dto) {
        Integer usableInventoryTotal = inventoryService.getUsableInventoryTotal(dto.getOrgId(), dto.getWarehouseId(), dto.getSkuId(), dto.getWarehouseLocationId());
        return success(usableInventoryTotal);
    }

}