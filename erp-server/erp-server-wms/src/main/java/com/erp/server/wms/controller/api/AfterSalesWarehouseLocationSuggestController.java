package com.erp.server.wms.controller.api;

import cn.hutool.core.collection.CollUtil;
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
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.AfterSalesWarehouseLocationSuggestEntity;
import com.erp.server.wms.query.AfterSalesWarehouseLocationSuggestQueryHandler;
import com.erp.server.wms.service.AfterSalesWarehouseLocationSuggestService;
import com.erp.server.wms.service.WarehouseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
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
@RequestMapping("/afterSalesWarehouseLocationSuggest")
    public class AfterSalesWarehouseLocationSuggestController extends BaseController {

    @Resource
    private AfterSalesWarehouseLocationSuggestService afterSalesWarehouseLocationSuggestService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 高级查询
     *
     * @param pagingDTO 查询参数
     * @return 查询列表
     * @date 2026-04-30
     * @author liuchao
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            menuCode = "wms:afterSalesWarehouseLocationSuggest:paging"
    )
    @WebAdvanceQuery(handler = AfterSalesWarehouseLocationSuggestQueryHandler.class)
    public ApiResult<PagingVO<AfterSalesWarehouseLocationSuggestDto.ListDTO>> paging(@RequestBody PagingDTO<AfterSalesWarehouseLocationSuggestDto.SearchParamDTO> pagingDTO) {
        PagingVO<AfterSalesWarehouseLocationSuggestDto.ListDTO> pagingResult = afterSalesWarehouseLocationSuggestService.paging(pagingDTO);
        return ApiResult.success(pagingResult);
    }

    /**
     * 获取默认的新增数据的仓库信息
     *
     * @return com.common.core.controller.vo.ApiResult
     * @date 2026-04-30
     * @author liuchao
     */
    @GetMapping("/getDefaultAddWarehouse")
    public ApiResult<WarehouseDTO.ListDTO> getDefaultAddData() {
        List<WarehouseDTO.ListDTO> dtos = warehouseService.listByNames(Collections.singletonList("东莞售后仓库"));
        return CollUtil.isNotEmpty(dtos)?ApiResult.success(dtos.get(0)):new ApiResult<>();
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
    public ApiResult add(@RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.AddOrEditDTO dto) {
        Boolean flag = afterSalesWarehouseLocationSuggestService.addOrEdit(dto);
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
            menuCode = "wms:afterSalesWarehouseLocationSuggest:update",
            serviceClass = AfterSalesWarehouseLocationSuggestService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.AddOrEditDTO dto) {
        Boolean flag = afterSalesWarehouseLocationSuggestService.addOrEdit(dto);
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
    public ApiResult<List<BatchResultDTO>> removeByIds(@RequestBody AfterSalesWarehouseLocationSuggestDto.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<AfterSalesWarehouseLocationSuggestEntity> entities = afterSalesWarehouseLocationSuggestService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            AfterSalesWarehouseLocationSuggestEntity entity = entities.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                resultDTO = BatchResultDTO.fail(id, id, "售后推荐仓位不存在, 批量删除失败");
                resultDTOS.add(resultDTO);
                continue;
            }
            try {
                resultDTO = afterSalesWarehouseLocationSuggestService.delete(entity);
            } catch (Exception e) {
                log.error("售后推荐仓位不存在, 删除售后推荐仓位失败", e);
                resultDTO = BatchResultDTO.fail(entity.getId(), null, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


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
        afterSalesWarehouseLocationSuggestService.importExcel(file, response);
        return ApiResult.success();
    }

    /**
     * 导出仓位售后推荐Excel（请求体与列表分页一致：含 {@code params}、{@code params.advanceQueryDTOList}、{@code params.ids} 等）
     *
     * @param body 原始 JSON，兼容仅有查询条件平铺在根节点的旧写法
     * @return 导出结果
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出仓位售后推荐Excel")
    public ApiResult<Boolean> exportExcel(@RequestBody JsonNode body) {
        afterSalesWarehouseLocationSuggestService.exportExcel(parseExportPagingRequest(body));
        return success(true);
    }

    private PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> parseExportPagingRequest(JsonNode body) {
        if (body == null || body.isNull()) {
            throw new ServiceException("导出参数不能为空");
        }
        try {
            if (body.hasNonNull("params")) {
                PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> paging =
                        objectMapper.convertValue(body, new TypeReference<PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO>>() {
                        });
                mergeRootLevelIdsIntoExportParams(body, paging.getParams());
                return paging;
            }
            PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> dto = new PagingDTO<>();
            dto.setCurrPage(body.hasNonNull("currPage") ? body.get("currPage").asInt() : 1);
            dto.setPageSize(body.hasNonNull("pageSize") ? body.get("pageSize").asInt() : 30);
            dto.setIsSearchCount(!body.has("isSearchCount") || body.get("isSearchCount").asBoolean());
            AfterSalesWarehouseLocationSuggestDto.ExportParamDTO params =
                    objectMapper.convertValue(body, AfterSalesWarehouseLocationSuggestDto.ExportParamDTO.class);
            dto.setParams(params);
            return dto;
        } catch (IllegalArgumentException e) {
            log.warn("导出参数解析失败", e);
            throw new ServiceException("导出参数解析失败");
        }
    }

    /**
     * 部分前端把勾选 id 放在分页根节点 {@code ids}，合并进 {@code params.ids} 供导出与列表 SQL 一致。
     */
    private void mergeRootLevelIdsIntoExportParams(JsonNode body, AfterSalesWarehouseLocationSuggestDto.ExportParamDTO params) {
        if (params == null || !body.has("ids") || body.get("ids").isNull() || !body.get("ids").isArray()) {
            return;
        }
        List<String> rootIds = objectMapper.convertValue(body.get("ids"), new TypeReference<List<String>>() {
        });
        if (rootIds != null && !rootIds.isEmpty()) {
            params.setIds(rootIds);
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        afterSalesWarehouseLocationSuggestService.downloadTemplate(response);
    }

    /**
     * 启用/禁用仓位
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 id={id},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<Void> updateStatus(@RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.UpdateStatusDto dto) {
        afterSalesWarehouseLocationSuggestService.updateDisabled(dto);
        return ApiResult.success();
    }

    /**
     * 批量启用/禁用仓位
     */
    @PostMapping("/updateStatusBatch")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 id={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<List<BatchResultDTO>> updateStatusBatch(@RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.UpdateStatusDto dto) {
        List<BatchResultDTO> resultDTOList = afterSalesWarehouseLocationSuggestService.updateStatusBatch(dto);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

}
