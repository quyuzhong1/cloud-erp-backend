package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.server.wms.query.WarehouseLocationReplenishQueryHandler;
import com.erp.server.wms.service.WarehouseLocationReplenishService;
import com.erp.server.wms.service.WarehouseLocationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 仓位补货
 * @date 2024-06-24
 * @author tanmujin
 */
@RestController
@RequestMapping("/warehouseLocationReplenish")
@LogSystemModule("仓位补货")
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "warehouse_id",
            menuCode = "wms:warehouseLocationReplenish:paging"
    )
    @WebAdvanceQuery(handler = WarehouseLocationReplenishQueryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationReplenishDTO.ViewDTO>> paging(@RequestBody PagingDTO<WarehouseLocationReplenishDTO.SearchParamDTO> pagingDTO){
        PagingVO<WarehouseLocationReplenishDTO.ViewDTO> pagingResult = replenishService.paging(pagingDTO);
        return ApiResult.success(pagingResult);
    }

    /**
     * 无需处理
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出补货清单")
    public ApiResult<?> exportExcel(@RequestBody WarehouseLocationReplenishDTO.ExportParamDTO dto){
        Boolean flag = replenishService.exportExcel(dto);
        return flag ? success() : failure();
    }

    /**
     * 批量处理
     * @param dtoList 数据清单
     * @return 批量处理结果
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/handleBatch")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量处理")
    public ApiResult<List<BatchResultDTO>> handleBatch(@RequestBody List<WarehouseLocationReplenishDTO.HandleDTO> dtoList){
        List<BatchResultDTO> verifyResultList = replenishService.verifyReplenishQty(dtoList);
        boolean verifyAllMatch = verifyResultList.stream().allMatch(BatchResultDTO::getSuccess);
        if(! verifyAllMatch){
            return failure(verifyResultList);
        }
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增补货单")
    public ApiResult<BatchResultDTO> add(@RequestBody WarehouseLocationReplenishDTO.AddDTO addDTO){
        BatchResultDTO resultDTO = replenishService.add(addDTO);
        return resultDTO.getSuccess() ? success(resultDTO) : failure(resultDTO);
    }

    /**
     * tabList
     */
    @GetMapping("/tabList")
    public ApiResult<List<WarehouseLocationReplenishDTO.TabDTO>> tabList(){
        List<WarehouseLocationReplenishDTO.TabDTO> tabDtoList = replenishService.listTabInfo();
        return ApiResult.success(tabDtoList);
    }
    /**
     * 查询库区
     * 库区类型 /wms/dict/drop/down?type=warehouseAreaType
     * @param idsDTO 仓库ID
     * @return 库区列表
     * @date: 2024-06-24
     * @author: tanmujin
     */
    @PostMapping("/listArea")
    public ApiResult<List<WarehouseLocationDTO.ReplenishAreaDTO>> listArea(@RequestBody BaseIdsDTO.IdsDTO idsDTO){
        List<WarehouseLocationDTO.ReplenishAreaDTO> list = warehouseLocationService.listArea(idsDTO);
        return ApiResult.success(list);
    }

    /**
     * 查询仓位
     * @date: 2024-06-24
     * @author: tanmujin
     */
    @PostMapping("/listLocationQty")
    public ApiResult<List<WarehouseLocationReplenishDTO.LocationQtyDTO>> listLocationQty(@RequestBody List<WarehouseLocationReplenishDTO.LocationQtyDTO> paramlist){
        List<WarehouseLocationReplenishDTO.LocationQtyDTO> list = replenishService.listLocationQty(paramlist);
        return ApiResult.success(list);
    }


    @PostMapping("/finishBatch")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量状态变更finish")
    public ApiResult<List<BatchResultDTO>> finishBatch(@RequestBody List<WarehouseLocationReplenishDTO.HandleDTO> dtoList){
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (WarehouseLocationReplenishDTO.HandleDTO handleDTO : dtoList) {
            BatchResultDTO finish = replenishService.finish(handleDTO);
            resultDTOS.add(finish);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 手动触发安全库存补货
     * @date: 2024-09-26
     * @author: jack
     */
    @GetMapping("/manualtriggerGenerateReplenishBill")
    public ApiResult<List<BatchResultDTO>> manualtriggerGenerateReplenishBill(){
        List<BatchResultDTO> resultDTOS = replenishService.generateReplenishBill();
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
