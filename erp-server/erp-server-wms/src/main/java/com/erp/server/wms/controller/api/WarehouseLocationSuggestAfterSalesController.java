package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WarehouseLocationSuggestAfterSalesDto;
import com.erp.model.wms.entity.WarehouseLocationSuggestAfterSalesEntity;
import com.erp.server.wms.query.WarehouseLocationSuggestAfterSalesQueryHandler;
import com.erp.server.wms.service.WarehouseLocationSuggestAfterSalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 仓位售后推荐表
 *
 * @author liuchao
 * @since 2026-04-30
 */
@Slf4j
@RestController
@LogSystemModule("仓位售后推荐表")
@RequestMapping("/warehouseLocationSuggestAfterSales")
public class WarehouseLocationSuggestAfterSalesController extends BaseController {

    @Resource
    private WarehouseLocationSuggestAfterSalesService warehouseLocationSuggestAfterSalesService;

    /**
     * 高级查询
     *
     * @param pagingDTO 查询参数
     * @return 查询列表
     * @date 2026-04-30
     * @author liuchao
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WarehouseLocationSuggestAfterSalesQueryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationSuggestAfterSalesDto.ListDTO>> paging(@RequestBody PagingDTO<WarehouseLocationSuggestAfterSalesDto.SearchParamDTO> pagingDTO) {
        PagingVO<WarehouseLocationSuggestAfterSalesDto.ListDTO> pagingResult = warehouseLocationSuggestAfterSalesService.paging(pagingDTO);
        return ApiResult.success(pagingResult);
    }


    /**
     * 新增仓位售后推荐
     *
     * @param dto 新增参数
     * @return com.common.core.controller.vo.ApiResult
     * @date 2026-04-30
     * @author liuchao
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓位售后推荐")
    public ApiResult add(@RequestBody @Validated WarehouseLocationSuggestAfterSalesDto.AddOrEditDTO dto) {
        Boolean flag = warehouseLocationSuggestAfterSalesService.addOrEdit(dto);
        return flag ? success() : failure();
    }

    /**
     * 修改仓位售后推荐
     *
     * @param dto 修改参数
     * @return com.common.core.controller.vo.ApiResult
     * @date 2026-04-30
     * @author liuchao
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改仓位售后推荐")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationSuggestAfterSales:update",
            serviceClass = WarehouseLocationSuggestAfterSalesService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseLocationSuggestAfterSalesDto.AddOrEditDTO dto) {
        Boolean flag = warehouseLocationSuggestAfterSalesService.addOrEdit(dto);
        return flag ? success() : failure();
    }

    /**
     * 批量删除仓位售后推荐
     *
     * @param dto 删除参数ids
     * @date 2026-04-30
     * @author liuchao
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除仓位售后推荐")
    public ApiResult<List<BatchResultDTO>> removeByIds(@RequestBody WarehouseLocationSuggestAfterSalesDto.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<WarehouseLocationSuggestAfterSalesEntity> entities = warehouseLocationSuggestAfterSalesService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            WarehouseLocationSuggestAfterSalesEntity entity = entities.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                resultDTO = BatchResultDTO.fail(id, id, "售后推荐仓位不存在, 批量删除失败");
                resultDTOS.add(resultDTO);
                continue;
            }
            try {
                resultDTO = warehouseLocationSuggestAfterSalesService.delete(entity);
            } catch (Exception e) {
                log.error("售后推荐仓位不存在, 删除售后推荐仓位失败", e);
                resultDTO = BatchResultDTO.fail(entity.getId(), null, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

//    /**
//     * 获取仓位售后推荐表
//     *
//     * @param id
//     * @return
//     */
//    @RequestMapping("/getById")
//    public WarehouseLocationSuggestAfterSalesEntity getById(String id) {
//        return warehouseLocationSuggestAfterSalesService.getById(id);
//    }

    /**
     * 导入仓位售后推荐Excel
     *
     * @param file     文件
     * @param response 响应
     * @return 导入结果
     */
    @PostMapping("/importExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入仓位售后推荐Excel")
    public ApiResult<Void> importExcel(@RequestParam("excelFile") MultipartFile file, HttpServletResponse response) {
        warehouseLocationSuggestAfterSalesService.importExcel(file, response);
        return ApiResult.success();
    }

    /**
     * 导出仓位售后推荐Excel
     *
     * @param dto 导出参数
     * @return 导出结果
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出仓位售后推荐Excel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated WarehouseLocationSuggestAfterSalesDto.ExportParamDTO dto) {
        warehouseLocationSuggestAfterSalesService.exportExcel(dto);
        return success(true);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        warehouseLocationSuggestAfterSalesService.downloadTemplate(response);
    }

}
