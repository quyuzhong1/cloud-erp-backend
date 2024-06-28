package com.erp.server.wms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.server.wms.query.WarehouseLocationReplenishQueryHandler;
import com.erp.server.wms.service.WarehouseLocationReplenishService;
import com.erp.server.wms.service.WarehouseLocationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 仓位补货
 * @date 2024-06-24
 * @author tanmujin
 */
@RestController
@RequestMapping("/warehouseLocationReplenish")
public class WarehouseLocationReplenishController extends BaseController {

    @Resource
    private WarehouseLocationReplenishService replenishService;
    @Resource
    private WarehouseLocationService warehouseLocationService;

    /**
     * 分页查询
     * @param pagingDTO
     * @return 查询列表
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WarehouseLocationReplenishQueryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationReplenishDTO.ViewDTO>> paging(@RequestBody PagingDTO<WarehouseLocationReplenishDTO.SearchParamDTO> pagingDTO){
        PagingVO<WarehouseLocationReplenishDTO.ViewDTO> pagingResult = replenishService.paging(pagingDTO);
        return ApiResult.success(pagingResult);
    }

    /**
     * 取消处理（批量）
     * @param dto
     * @return 批量处理结果
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/cancelHandleBatch")
    public ApiResult<List<BatchResultDTO>> cancelHandleBatch(@RequestBody BaseIdsDTO.IdsDTO dto){
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result = replenishService.cancelHandle(id);
            if(result != null){
                resultList.add(result);
            }
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 导出补货清单
     * @param dto 导出参数
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery(handler = WarehouseLocationReplenishQueryHandler.class)
    public ApiResult<?> exportExcel(@RequestBody WarehouseLocationReplenishDTO.ExportParamDTO dto, HttpServletResponse response){
        Boolean flag = replenishService.exportExcel(dto, response);
        return flag ? success() : failure();
    }

    /**
     * 处理补货单（批量）
     * @param dtoList 数据清单
     * @return 批量处理结果
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/handleBatch")
    public ApiResult<List<BatchResultDTO>> handleBatch(@RequestBody List<WarehouseLocationReplenishDTO.HandleDTO> dtoList){
        List<BatchResultDTO> resultList = new ArrayList<>(dtoList.size());
        for (WarehouseLocationReplenishDTO.HandleDTO dto : dtoList) {
            BatchResultDTO resultDTO = replenishService.handle(dto);
            resultList.add(resultDTO);
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    @PostMapping("/finishBatch")
    public ApiResult<List<BatchResultDTO>> finishBatch(@RequestBody List<WarehouseLocationReplenishDTO.HandleDTO> dtoList){
        List<BatchResultDTO> resultList = new ArrayList<>(dtoList.size());
        for (WarehouseLocationReplenishDTO.HandleDTO dto : dtoList) {
            BatchResultDTO resultDTO = replenishService.finish(dto);
            resultList.add(resultDTO);
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 新增补货单
     * @param addDTO
     * @return BatchResultDTO
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/add")
    public ApiResult<BatchResultDTO> add(@RequestBody WarehouseLocationReplenishDTO.AddDTO addDTO){
        BatchResultDTO resultDTO = replenishService.add(addDTO);
        return resultDTO.getSuccess() ? success(resultDTO) : failure(resultDTO);
    }

    @GetMapping("/tabList")
    public ApiResult<List<WarehouseLocationReplenishDTO.TabDTO>> tabList(){
        List<WarehouseLocationReplenishDTO.TabDTO> tabDtoList = replenishService.listTabInfo();
        return ApiResult.success(tabDtoList);
    }
    /**
     * 根据仓库id和库区类型 查询库区
     * @param warehouseId 仓库ID
     * @param areaTypeCode 库区类型编码 /wms/dict/drop/down?type=warehouseAreaType
     * @return 库区列表
     * @date: 2024-06-24
     * @author: tanmujin
     */
    @GetMapping("/listArea")
    public ApiResult<List<WarehouseLocationDTO.CoreDTO>> listArea(@RequestParam String warehouseId, @RequestParam String areaTypeCode){
        List<WarehouseLocationDTO.CoreDTO> list = warehouseLocationService.listArea(warehouseId, areaTypeCode);
        return ApiResult.success(list);
    }

    /**
     * 查询仓位及仓位下的SKU数量
     * @param skuNo sku编码
     * @param warehouseId 仓库ID
     * @param warehouseAreaCode 库区编码
     * @return 仓位，sku，以及关联的库区
     * @date: 2024-06-24
     * @author: tanmujin
     */
    @GetMapping("/listLocationQty")
    public ApiResult<List<WarehouseLocationReplenishDTO.LocationQtyDTO>> listLocationQty(@RequestParam String warehouseId, @RequestParam String warehouseAreaCode, @RequestParam String skuNo){
        List<WarehouseLocationReplenishDTO.LocationQtyDTO> list = replenishService.listLocationQty(warehouseId, warehouseAreaCode, skuNo);
        return ApiResult.success(list);
    }
}
