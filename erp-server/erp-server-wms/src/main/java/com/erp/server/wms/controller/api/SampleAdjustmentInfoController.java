package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.SampleAdjustmentInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleAdjustmentInfoDTO;
import com.erp.server.wms.query.SampleAdjustmentInfoQueryHandler;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SampleAdjustmentInfoEntity;

/**
 * 样品调整单
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
@Slf4j
@RestController
@LogSystemModule("样品调整单")
@RequestMapping("/sampleAdjustmentInfo")
public class SampleAdjustmentInfoController extends BaseController {

    @Resource
    private SampleAdjustmentInfoService sampleAdjustmentInfoService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品调整单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleAdjustmentInfoDTO.AddDTO dto) {
        return success(sampleAdjustmentInfoService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品调整单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleAdjustmentInfo:update",
        serviceClass = SampleAdjustmentInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleAdjustmentInfoDTO.UpdateDTO dto) {
        sampleAdjustmentInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:paging",
            tableAlias = "sai"
    )
    public ApiResult<List<SampleAdjustmentInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(sampleAdjustmentInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return ApiResult<PagingVO<SampleAdjustmentInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:paging",
            tableAlias = "sai"
    )
    @WebAdvanceQuery(handler = SampleAdjustmentInfoQueryHandler.class)
    public ApiResult<PagingVO<SampleAdjustmentInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> dto) {
        return success(sampleAdjustmentInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleAdjustmentInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = sampleAdjustmentInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:update",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleAdjustmentInfoDTO.UpdateDTO dto) {
        sampleAdjustmentInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author wuhaotian
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:submit",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品调整单提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleAdjustmentInfoEntity> list = sampleAdjustmentInfoService.lambdaQuery().in(SampleAdjustmentInfoEntity::getId, ids).list();
		Map<String, SampleAdjustmentInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleAdjustmentInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleAdjustmentInfoService.submit(id);
            }catch (Exception e){
                log.error("样品调整单 提交审核失败",e);
                SampleAdjustmentInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品调整单不存在, 提交失败");
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
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:approve",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品调整单审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleAdjustmentInfoEntity> list = sampleAdjustmentInfoService.lambdaQuery().in(SampleAdjustmentInfoEntity::getId, ids).list();
		Map<String, SampleAdjustmentInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleAdjustmentInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleAdjustmentInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("样品调整单审核失败",e);
                SampleAdjustmentInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品调整单不存在, 审核失败");
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
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:disApprove",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品调整单反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleAdjustmentInfoEntity> list = sampleAdjustmentInfoService.lambdaQuery().in(SampleAdjustmentInfoEntity::getId, ids).list();
		Map<String, SampleAdjustmentInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleAdjustmentInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleAdjustmentInfoService.disApprove(id);
            }catch (Exception e){
                log.error("样品调整单反审核失败",e);
                SampleAdjustmentInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品调整单不存在, 反审核失败");
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
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:delete",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品调整单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleAdjustmentInfoEntity> list = sampleAdjustmentInfoService.lambdaQuery().in(SampleAdjustmentInfoEntity::getId, ids).list();
		Map<String, SampleAdjustmentInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleAdjustmentInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleAdjustmentInfoService.delete(id);
            }catch (Exception e){
                log.error("样品调整单删除失败",e);
                SampleAdjustmentInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品调整单不存在, 删除失败");
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
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:invalid",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品调整单作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleAdjustmentInfoEntity> list = sampleAdjustmentInfoService.lambdaQuery().in(SampleAdjustmentInfoEntity::getId, ids).list();
		Map<String, SampleAdjustmentInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleAdjustmentInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = sampleAdjustmentInfoService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("样品调整单作废失败",e);
                SampleAdjustmentInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "样品调整单不存在, 作废失败");
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
    * @date:  2025-11-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:cancelProcess",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品调整单撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SampleAdjustmentInfoEntity> list = sampleAdjustmentInfoService.lambdaQuery().in(SampleAdjustmentInfoEntity::getId, ids).list();
        Map<String, SampleAdjustmentInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleAdjustmentInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleAdjustmentInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("样品调整单撤回流程失败",e);
                SampleAdjustmentInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品调整单不存在, 撤回流程失败");
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
    * @date:  2025-11-14
    * @param id
    * @return ApiResult<SampleAdjustmentInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:view",
            serviceClass = SampleAdjustmentInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SampleAdjustmentInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleAdjustmentInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wuhaotian
    * @date:  2025-11-14
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleAdjustmentInfo:export",
            tableAlias = "sai"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品调整单导出Excel数据")
    @WebAdvanceQuery(handler = SampleAdjustmentInfoQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated SampleAdjustmentInfoDTO.ExportDTO dto, HttpServletResponse response) {
        sampleAdjustmentInfoService.exportList(dto, response);
        return success();
    }

    /**
     *  异步导入
     * @author wuhaotian
     * @date:  2025-11-14
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入样品调整单")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = sampleAdjustmentInfoService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-11-14
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品调整单导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        sampleAdjustmentInfoService.downloadTemplate(response);
        return success();
    }

}
