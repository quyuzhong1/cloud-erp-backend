package com.erp.server.tms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.server.tms.query.FirstMileWeightAllocationQueryHandler;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
    @Resource
    private FirstMileChangeRecordService firstMileChangeRecordService;
    /**
     * 分页
     * @param dto
     * @author tmj
     * @date 2024-8-22
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "wa.shop_id",
            warehouseTableField = "wa.from_warehouse_id",
            menuCode = "tms:firstMileWeightAllocation:paging",
            tableAlias = "wa"
    )
    @WebAdvanceQuery(handler = FirstMileWeightAllocationQueryHandler.class)
    public ApiResult<PagingVO<FirstMileWeightAllocationDTO.ViewDTO>> paging(@RequestBody @Valid PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        PagingVO<FirstMileWeightAllocationDTO.ViewDTO> pagingVO = firstMileWeightAllocationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出Excel
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程重量分摊导出")
    @PostMapping("/exportExcel")
    public ApiResult<Object> exportExcel(@RequestBody FirstMileWeightAllocationDTO.ExportParamDTO dto){
        firstMileWeightAllocationService.exportExcel(dto);
        return ApiResult.success();
    }

    /**
     * 统计tab数量
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "wa.shop_id",
            warehouseTableField = "wa.from_warehouse_id",
            menuCode = "tms:firstMileWeightAllocation:paging",
            tableAlias = "wa"
    )
    public ApiResult<List<FirstMileWeightAllocationDTO.TabDTO>> tabList(@RequestBody FirstMileWeightAllocationDTO.PagingParamDTO pagingParamDTO){
        List<FirstMileWeightAllocationDTO.TabDTO> list = firstMileWeightAllocationService.tabList(pagingParamDTO);
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
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除")
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

    /**
     * 修改单产品重量预览
     * @param dto
     * @return
     */
    @PostMapping("/viewProductWeight")
    public ApiResult<List<FirstMileWeightAllocationDTO.ViewProductWeightDTO>> viewProductWeight(@RequestBody FirstMileWeightAllocationDTO.ViewProductWeightParamDTO dto){
        List<FirstMileWeightAllocationDTO.ViewProductWeightDTO> list = firstMileWeightAllocationService.viewProductWeight(dto);
        return success(list);
    }
    /**
     * 修改单产品重量保存
     * @param dtoValidList
     * @return
     */
    @PostMapping("/changeProductWeight")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改单产品重量保存")
    public ApiResult<List<BatchResultDTO>> changeProductWeight(@RequestBody @Valid ValidList<FirstMileWeightAllocationDTO.ProductWeightDTO> dtoValidList){
        //批量校验是否存在相同维度的sku修改数据
        List<BatchResultDTO> batchResultDTOS = firstMileChangeRecordService.checkSameDimension(dtoValidList);
        //存在异常校验直接返回
        if (batchResultDTOS.stream().anyMatch(item -> !item.getSuccess())) {
            return failure(batchResultDTOS);
        }
        //批量保存修改记录
        firstMileChangeRecordService.saveProductWeight(dtoValidList);
        //按照保存成功记录，进行按照单据进行重新重量分摊
        List<String> logisticsBillIds = dtoValidList.stream().map(FirstMileWeightAllocationDTO.ViewProductWeightDTO::getLogisticsBillId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        for (String logisticsBillId : logisticsBillIds) {
            BatchResultDTO resultDTO = firstMileWeightAllocationService.weightReCompute(logisticsBillId);
            batchResultDTOS.add(resultDTO);
        }
        return batchResultDTOS.stream().allMatch(BatchResultDTO::getSuccess)? success(batchResultDTOS) : failure(batchResultDTOS);
    }
    /**
     * 修改出库重量/尺寸
     * @param dtoValidList
     * @return
     */
    @PostMapping("/changePackageWeight")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改出库重量/尺寸")
    public ApiResult<List<BatchResultDTO>> changePackageWeight(@RequestBody @Valid ValidList<FirstMileWeightAllocationDTO.PackageSizeDTO> dtoValidList){
        //批量校验是否存在相同维度的sku修改数据
        List<BatchResultDTO> batchResultDTOS = firstMileChangeRecordService.checkPackageSameDimension(dtoValidList);
        //存在异常校验直接返回
        if (batchResultDTOS.stream().anyMatch(item -> !item.getSuccess())) {
            return failure(batchResultDTOS);
        }
        //批量保存修改记录
        firstMileChangeRecordService.savePackageWeight(dtoValidList);
        //按照保存成功记录，进行按照单据进行重新重量分摊
        List<String> logisticsBillIds = dtoValidList.stream().map(FirstMileWeightAllocationDTO.ViewProductWeightDTO::getLogisticsBillId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        for (String logisticsBillId : logisticsBillIds) {
            BatchResultDTO resultDTO = firstMileWeightAllocationService.weightReCompute(logisticsBillId);
            batchResultDTOS.add(resultDTO);
        }
        return batchResultDTOS.stream().allMatch(BatchResultDTO::getSuccess)? success(batchResultDTOS) : failure(batchResultDTOS);
    }
    /**
     * 下载重量分摊调整导入模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载重量分摊调整导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        firstMileWeightAllocationService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入重量分摊调整
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入重量分摊调整")
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = firstMileWeightAllocationService.importExcel(excelFile, response);
        return result?success():failure();
    }
}
