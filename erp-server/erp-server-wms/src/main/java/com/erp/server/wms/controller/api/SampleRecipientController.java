package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.ClientTypeEnum;
import com.erp.server.wms.query.SampleBackInfoQueryHandler;
import com.erp.server.wms.query.SampleRecipientQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleRecipientService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleRecipientDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SampleRecipientEntity;

/**
 * 样品领用单
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品领用单")
@RequestMapping("/sampleRecipient")
public class SampleRecipientController extends BaseController {

    @Resource
    private SampleRecipientService sampleRecipientService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品领用单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleRecipientDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.WEB);
        return success(sampleRecipientService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品领用单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleReceipt:update",
        serviceClass = SampleRecipientService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleRecipientDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.WEB);
        sampleRecipientService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:paging",
            tableAlias = ""
    )
    public ApiResult<List<SampleRecipientDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(sampleRecipientService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return ApiResult<PagingVO<SampleRecipientDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SampleRecipientQueryHandler.class)
    public ApiResult<PagingVO<SampleRecipientDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleRecipientDTO.PagingParamDTO> dto) {
        return success(sampleRecipientService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleRecipientDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.WEB);
        BaseResultDTO.AddDTO result = sampleRecipientService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:updateAndSubmit",
            serviceClass = SampleRecipientService.class,
            keyIdName = "id")
        public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleRecipientDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.WEB);
        sampleRecipientService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:submit",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品领用单提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
		Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleRecipientService.submit(id);
            }catch (Exception e){
                log.error("样品领用单 提交审核失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品领用单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 审核
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:approve",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品领用单审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
		Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleRecipientService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("样品领用单审核失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:disApprove",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品领用单反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
		Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleRecipientService.disApprove(id);
            }catch (Exception e){
                log.error("样品领用单反审核失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:delete",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品领用单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
		Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleRecipientService.delete(id);
            }catch (Exception e){
                log.error("样品领用单删除失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
    * 作废
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:invalid",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品领用单作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
		Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = sampleRecipientService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("样品领用单作废失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:cancelProcess",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品领用单撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleRecipientService.cancelProcess(id);
            }catch (Exception e){
                log.error("样品领用单撤回流程失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author wuhaotian
    * @date:  2025-08-21
    * @param id
    * @return ApiResult<SampleRecipientDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:view",
            serviceClass = SampleRecipientService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SampleRecipientDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleRecipientService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品领用单导出Excel数据")
    @WebAdvanceQuery(handler = SampleRecipientQueryHandler.class)
    public ApiResult<Boolean> exportList(@RequestBody @Validated SampleRecipientDTO.ExportDTO dto, HttpServletResponse response) {
        sampleRecipientService.exportList(dto, response);
        return success(true);
    }

    /**
    * 结束领用
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/finishRecipient")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:finishRecipient",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品领用单结束领用")
    public ApiResult<List<BatchResultDTO>> batchFinishRecipient(@RequestBody @Validated SampleRecipientDTO.FinishRecipientDTO dto) {
        List<BatchResultDTO> resultDTOS = sampleRecipientService.finishRecipient(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 查询SKU成本
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<SampleRecipientDTO.SkuDTO>>
    */
    @PostMapping("/querySkuCost")
    public ApiResult<List<SampleRecipientDTO.SkuDTO>> querySkuCost(@RequestBody @Validated SampleRecipientDTO.SkuCostQueryDTO dto) {
        List<SampleRecipientDTO.SkuDTO> result = sampleRecipientService.querySkuCost(dto);
        return success(result);
    }

    /**
    * 获取SKU可领用库存
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<SampleRecipientDTO.SkuAvailableStockDTO>>
    */
    @PostMapping("/querySkuAvailableStock")
    public ApiResult<List<SampleRecipientDTO.SkuAvailableStockDTO>> querySkuAvailableStock(@RequestBody @Validated SampleRecipientDTO.SkuAvailableStockQueryDTO dto) {
        List<SampleRecipientDTO.SkuAvailableStockDTO> result = sampleRecipientService.querySkuAvailableStock(dto);
        return success(result);
    }
    
    /**
     * 获取SKU列表（支持高级查询和模糊搜索）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return ApiResult<PagingVO<SampleRecipientDTO.SkuListResponseDTO>>
     */
    @PostMapping("/getSkuList")
    public ApiResult<PagingVO<SampleRecipientDTO.SkuListResponseDTO>> getSkuList(@RequestBody @Validated SampleRecipientDTO.SkuListQueryDTO dto) {
        PagingVO<SampleRecipientDTO.SkuListResponseDTO> result = sampleRecipientService.getSkuList(dto);
        return success(result);
    }

    /**
     * 下推其他出库单查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return ApiResult<List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO>>
     */
    @PostMapping("/viewGenerateOutboundOrder")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleReceipt:generateOutboundOrder",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    public ApiResult<List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO>> viewGenerateOutboundOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> list = sampleRecipientService.viewGenerateOutboundOrder(dto.getIds());
        return success(list);
    }

    /**
     * 下推其他出库单保存
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/generateOutboundOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推其他出库单保存")
    public ApiResult<List<BatchResultDTO>> generateOutboundOrder(@RequestBody @Validated SampleRecipientDTO.ListGenerateOutboundOrderDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getList().size());
        try {
            resultDTOS = sampleRecipientService.generateOutboundOrder(dto);
        } catch (Exception e) {
            log.error("下推其他出库单保存失败", e);
            BatchResultDTO resultDTO = BatchResultDTO.fail(dto.getList().get(0).getSourceId(), "", e.getMessage());
            resultDTOS.add(resultDTO);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-08-21
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品领用单导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        sampleRecipientService.downloadTemplate(response);
        return success();
    }

    /**
     * 异步导入
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/importExcel")
    public ApiResult<Object> importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean flag = sampleRecipientService.importExcel(dto);
        return flag == true ? success() : failure();
    }

}
