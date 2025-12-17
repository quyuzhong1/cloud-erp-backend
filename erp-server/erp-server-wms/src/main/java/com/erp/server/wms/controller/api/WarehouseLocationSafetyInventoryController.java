package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDTO;
import com.erp.server.wms.query.WarehouseLocationSafetyInventoryHandler;
import com.erp.server.wms.service.WarehouseLocationSafetyInventoryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 仓位安全库存
 * @date 2024-06-21
 * @author tanmujin
 */
@RestController
@RequestMapping("/warehouseLocationSafetyInventory")
@LogSystemModule("仓位安全库存")
public class WarehouseLocationSafetyInventoryController extends BaseController {

    @Resource
    private WarehouseLocationSafetyInventoryService safetyInventoryService;

    /**
     * 高级查询
     * @param pagingDTO 查询参数
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "warehouse_id",
            menuCode = "wms:warehouseLocationSafetyInventory:paging"
    )
    @WebAdvanceQuery(handler = WarehouseLocationSafetyInventoryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO>> paging(@RequestBody PagingDTO<WarehouseLocationSafetyInventoryDTO.SearchParamDTO> pagingDTO){
        PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> pagingResult = safetyInventoryService.paging(pagingDTO);
        return ApiResult.success(pagingResult);
    }

    /**
     * 批量更新
     * @param list 更新的列表
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/updateBatch")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量更新 id={id} 安全库存={safetyQty},补货上限={maxQty}")
    public ApiResult<List<BaseResultDTO.UpdateDTO>> updateBatch(@RequestBody List<WarehouseLocationSafetyInventoryDTO.UpdateParamDTO> list){
        List<BaseResultDTO.UpdateDTO> resultList = new ArrayList<>(list.size());
        for (WarehouseLocationSafetyInventoryDTO.UpdateParamDTO dto : list) {
            BaseResultDTO.UpdateDTO result = safetyInventoryService.updateInventory(dto);
            if(result != null){
                resultList.add(result);
            }
        }
        return ApiResult.success(resultList);
    }

    /**
     * 导入Excel
     * @param file 导入的文件
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/importExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    public ApiResult importExcel(@RequestParam("excelFile") MultipartFile file, HttpServletResponse response){
        boolean flag = safetyInventoryService.importExcel(file, response);
        return flag ? success() : failure();
    }

    /**
     * 导出仓位安全库存
     * @param dto 导出参数
     * @return void
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出仓位安全库存")
    @WebAdvanceQuery(handler = WarehouseLocationSafetyInventoryHandler.class)
    public ApiResult exportExcel(@RequestBody WarehouseLocationSafetyInventoryDTO.exportParamDTO dto){
        boolean flag = safetyInventoryService.exportExcel(dto);
        return flag ? success() : failure();
    }

    /**
     * 下载导入模板
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response){
        safetyInventoryService.downloadTemplate(response);
        return success();
    }
}
