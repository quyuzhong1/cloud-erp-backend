package com.erp.server.wms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDto;
import com.erp.server.wms.query.WarehouseLocationSafetyInventoryHandler;
import com.erp.server.wms.service.WarehouseLocationSafetyInventoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
public class WarehouseLocationSafetyInventoryController {

    @Resource
    private WarehouseLocationSafetyInventoryService safetyInventoryService;

    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WarehouseLocationSafetyInventoryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationSafetyInventoryDto.ViewDto>> paging(@RequestBody PagingDTO<WarehouseLocationSafetyInventoryDto.SearchParamDto> pagingDTO){
        PagingVO<WarehouseLocationSafetyInventoryDto.ViewDto> pagingResult = safetyInventoryService.paging(pagingDTO);
        return ApiResult.success(pagingResult);
    }

    @PostMapping("/updateBatch")
    public ApiResult<List<BaseResultDTO.UpdateDTO>> updateBatch(@RequestBody List<WarehouseLocationSafetyInventoryDto.UpdateParamDto> list){
        List<BaseResultDTO.UpdateDTO> resultList = new ArrayList<>(list.size());
        for (WarehouseLocationSafetyInventoryDto.UpdateParamDto dto : list) {
            BaseResultDTO.UpdateDTO result = safetyInventoryService.updateInventory(dto);
            if(result != null){
                resultList.add(result);
            }
        }
        return ApiResult.success(resultList);
    }

    @PostMapping("/importExcel")
    public void importExcel(MultipartFile file, HttpServletResponse response){
        safetyInventoryService.importExcel(file, response);
    }

    @PostMapping("/exportExcel")
    public void exportExcel(WarehouseLocationSafetyInventoryDto.exportParamDto dto, HttpServletResponse response){
        safetyInventoryService.exportExcel(dto, response);
    }

    @PostMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response){
        ExcelUtil.downloadTemplate("excel/warehouseLocationSafetyInventoryImport.xlsx", "仓位安全库存导入模板.xlsx", response);
    }
}
