package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.erp.server.tms.query.InventorySkuCostQueryHandler;
import com.erp.server.tms.service.TmsFirstMileReconciliationService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.InventorySkuCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SKU成本
 *
 * @author zdy
 * @since 2024-08-16
 */
@Slf4j
@RestController
@LogSystemModule("SKU成本")
@RequestMapping("/inventorySkuCost")
public class InventorySkuCostController extends BaseController {

    @Resource
    private InventorySkuCostService inventorySkuCostService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-08-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "SKU成本新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated InventorySkuCostDTO.AddDTO dto) {
        return success(inventorySkuCostService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-08-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "SKU成本修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:inventorySkuCost:update",
        serviceClass = InventorySkuCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated InventorySkuCostDTO.UpdateDTO dto) {
        inventorySkuCostService.update(dto);
        return success();
    }


    /**
     * tab 列表
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:paging",
            tableAlias = "lb"
    )
    public ApiResult<List<InventorySkuCostDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<InventorySkuCostDTO.TabListDTO> tabList = inventorySkuCostService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = InventorySkuCostQueryHandler.class)
    public ApiResult<PagingVO<InventorySkuCostDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<InventorySkuCostDTO.PagingParamDTO> dto) {
        PagingVO<InventorySkuCostDTO.PagingVO> pagingVO = inventorySkuCostService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 批量审核
     * @author zdy
     * @date: 2024/8/15 17:54
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核SKU成本")
    @PostMapping("/approve")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "tms:inventorySkuCost:approve",
//            serviceClass = inventorySkuCostService.class,
//            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InventorySkuCostEntity> entityList = inventorySkuCostService.listByIds(ids);
        for (String id : ids) {
            InventorySkuCostEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"SKU成本记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(inventorySkuCostService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("SKU成本记录审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:disApprove",
            serviceClass = InventorySkuCostService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InventorySkuCostEntity> entityList = inventorySkuCostService.listByIds(ids);
        for (String id : ids) {
            InventorySkuCostEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"SKU成本记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(inventorySkuCostService.disApprove(entity));
            }catch (Exception e){
                log.error("SKU成本记录反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 撤销
     */
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:cancel",
            serviceClass = InventorySkuCostService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InventorySkuCostEntity> entityList = inventorySkuCostService.listByIds(ids);
        for (String id : ids) {
            InventorySkuCostEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"SKU成本记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(inventorySkuCostService.cancel(entity));
            }catch (Exception e){
                log.error("SKU成本记录撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 提交审核
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:submit",
            serviceClass = InventorySkuCostService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InventorySkuCostEntity> entityList = inventorySkuCostService.listByIds(ids);
        for (String id : ids) {
            InventorySkuCostEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"SKU成本记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(inventorySkuCostService.submit(entity));
            }catch (Exception e){
                log.error("SKU成本记录提交审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 修改并提交审核
     *
     * @param dto DTO
     * @return ApiResult<Void>
     * @author zdy
     * {@code @date:}2024-03-25
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:updateAndSubmit",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated InventorySkuCostDTO.UpdateDTO dto) {
        inventorySkuCostService.updateAndSubmit(dto);
        return success();
    }
    /**
     * 删除记录
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:delete",
            serviceClass = InventorySkuCostService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InventorySkuCostEntity> entityList = inventorySkuCostService.listByIds(ids);
        for (String id : ids) {
            InventorySkuCostEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"SKU成本记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(inventorySkuCostService.delete(entity));
            }catch (Exception e){
                log.error("SKU成本记录删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 导出Excel
     *
     * @param dto
     * @author zdy
     * @date 2024-8-15 10:54
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery(handler = InventorySkuCostQueryHandler.class)
    public ApiResult<?> exportExcel(@RequestBody @Valid InventorySkuCostDTO.PagingParamDTO dto) {
        inventorySkuCostService.exportExcel(dto);
        return success(Boolean.TRUE);
    }
    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载SKU成本导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        inventorySkuCostService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入Excel
     * @author zdy
     * @date: 2024/8/14 9:39
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    @PostMapping("/importFile")
    public ApiResult<InventorySkuCostDTO.ImportDTO> importFile(@ModelAttribute @Validated InventorySkuCostDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        InventorySkuCostDTO.ImportDTO dto = inventorySkuCostService.importFile(excelImportDTO.getExcelFile(),excelImportDTO.getDetailList(),response);
        return success(dto);
    }
    /**
     * 详情
     *
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:inventorySkuCost:view",
            tableAlias = "a"
    )
    public ApiResult<InventorySkuCostDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        InventorySkuCostDTO.ViewDTO result = inventorySkuCostService.view(dto.getId());
        return success(result);
    }


}
