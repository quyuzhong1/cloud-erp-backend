package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.server.tms.query.FirstMileWeightAllocationQueryHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 头程重量分摊
 *
 * @author tmj
 * @since 2024-08-20
 */
@Slf4j
@RestController
@LogSystemModule("头程重量分摊")
@RequestMapping("/firstMileWeightAllocation")
public class FirstMileWeightAllocationController extends BaseController {

    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;

    /**
     * 分页
     * @param dto
     * @author tmj
     * @date 2024-8-22
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:firstMileWeightAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = FirstMileWeightAllocationQueryHandler.class)
    public ApiResult<PagingVO<FirstMileWeightAllocationDTO.ViewDTO>> paging(@RequestBody @Valid PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        PagingVO<FirstMileWeightAllocationDTO.ViewDTO> pagingVO = firstMileWeightAllocationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出Excel
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery(handler = FirstMileWeightAllocationQueryHandler.class)
    public ApiResult<?> exportExcel(@RequestBody FirstMileWeightAllocationDTO.ExportParamDTO dto, HttpServletResponse response){
        firstMileWeightAllocationService.exportExcel(dto, response);
        return ApiResult.success();
    }

    /**
     * 统计tab数量
     */
    @GetMapping("/tabList")
    public ApiResult<List<FirstMileWeightAllocationDTO.TabDTO>> tabList(){
        List<FirstMileWeightAllocationDTO.TabDTO> list = firstMileWeightAllocationService.tabList();
        return ApiResult.success(list);
    }

    /**
     * 重量重算
     */
    @PostMapping("/weightReCompute")
    public ApiResult<List<BatchResultDTO>> weightReCompute(@RequestBody BaseIdsDTO.IdsDTO dto){
        List<FirstMileWeightAllocationEntity> entityList = firstMileWeightAllocationService.listByIds(dto.getIds());
        List<String> logisticsBillIds = entityList.stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<BatchResultDTO> list = new ArrayList<>(logisticsBillIds.size());
        for (String logisticsBillId : logisticsBillIds) {
            BatchResultDTO resultDTO = firstMileWeightAllocationService.weightReCompute(logisticsBillId);
            list.add(resultDTO);
        }
        return list.stream().allMatch(BatchResultDTO::getSuccess) ? success(list) : failure(list);
    }

    /**
     * 批量删除
     */
    @PostMapping("/deleteBatch")
    public ApiResult<List<BatchResultDTO>> deleteBatch(@RequestBody BaseIdsDTO.IdsDTO dto){
        List<FirstMileWeightAllocationEntity> entityList = firstMileWeightAllocationService.listByIds(dto.getIds());
        List<String> logisticsBillIds = entityList.stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<BatchResultDTO> list = new ArrayList<>(logisticsBillIds.size());
        for (String logisticsBillId : logisticsBillIds) {
            BatchResultDTO resultDTO = firstMileWeightAllocationService.deleteByLogisticsBillId(logisticsBillId);
            list.add(resultDTO);
        }
        return list.stream().allMatch(BatchResultDTO::getSuccess) ? success(list) : failure(list);
    }
}
