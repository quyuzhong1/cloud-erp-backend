package com.erp.server.wms.controller.api;


import com.common.business.enums.ClientTypeEnum;
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
import com.erp.server.wms.service.SampleTransferInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleTransferInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SampleTransferInfoEntity;
import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.wms.query.SampleTransferInfoQueryHandler;

/**
 * 样品转移单主表
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
@Slf4j
@RestController
@LogSystemModule("样品转移单主表")
@RequestMapping("/sampleTransferInfo")
public class SampleTransferInfoController extends BaseController {

    @Resource
    private SampleTransferInfoService sampleTransferInfoService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品转移单主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleTransferInfoDTO.AddDTO dto) {
        return success(sampleTransferInfoService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品转移单主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleTransferInfo:update",
        serviceClass = SampleTransferInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleTransferInfoDTO.UpdateDTO dto) {
        sampleTransferInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:paging",
            tableAlias = ""
    )
    public ApiResult<List<SampleTransferInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(sampleTransferInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return ApiResult<PagingVO<SampleTransferInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SampleTransferInfoQueryHandler.class)
    public ApiResult<PagingVO<SampleTransferInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleTransferInfoDTO.PagingParamDTO> dto) {
        return success(sampleTransferInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleTransferInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = sampleTransferInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:update",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleTransferInfoDTO.UpdateDTO dto) {
        sampleTransferInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:submit",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品转移单主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
		Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleTransferInfoService.submit(id,ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品转移单主单 提交审核失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品转移单主单不存在, 提交失败");
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
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:approve",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品转移单主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
		Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleTransferInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()),ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品转移单主单审核失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品转移单主单不存在, 审核失败");
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
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:disApprove",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品转移单主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
		Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleTransferInfoService.disApprove(id, ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品转移单主单反审核失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品转移单主单不存在, 反审核失败");
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
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:delete",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品转移单主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
		Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleTransferInfoService.delete(id, ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品转移单主单删除失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品转移单主单不存在, 删除失败");
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
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:invalid",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品转移单主表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
		Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = sampleTransferInfoService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("样品转移单主单作废失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "样品转移单主单不存在, 作废失败");
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
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:cancelProcess",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品转移单主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleTransferInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("样品转移单主单撤回流程失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品转移单主单不存在, 撤回流程失败");
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
    * @date:  2025-10-28
    * @param id
    * @return ApiResult<SampleTransferInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:view",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SampleTransferInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleTransferInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:export",
            tableAlias = "sti"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品转移单主表导出Excel数据")
    @WebAdvanceQuery(handler = SampleTransferInfoQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated SampleTransferInfoDTO.ExportDTO dto, HttpServletResponse response) {
        sampleTransferInfoService.exportList(dto, response);
        return success();
    }

    /**
     * 异步导入
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入样品转移单")
    @PostMapping("/importFile")
    public ApiResult<Object> importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = sampleTransferInfoService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-10-28
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品转移单下载模板")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        sampleTransferInfoService.downloadTemplate(response);
    }

}
