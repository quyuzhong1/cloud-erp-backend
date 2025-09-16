package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.wms.query.SampleInitialLedgerQueryHandler;
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
import com.erp.server.wms.service.SampleInitialLedgerService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleInitialLedgerDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;
import com.common.core.enums.*;

/**
 * 样品期初台账
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品期初台账")
@RequestMapping("/sampleInitialLedger")
public class SampleInitialLedgerController extends BaseController {

    @Resource
    private SampleInitialLedgerService sampleInitialLedgerService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品期初台账新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleInitialLedgerDTO.AddDTO dto) {
        return success(sampleInitialLedgerService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品期初台账修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleInitialLedger:update",
        serviceClass = SampleInitialLedgerService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleInitialLedgerDTO.UpdateDTO dto) {
        sampleInitialLedgerService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleInitialLedger:paging"
    )
    public ApiResult<List<SampleInitialLedgerDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(sampleInitialLedgerService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return ApiResult<PagingVO<SampleInitialLedgerDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleInitialLedger:paging",
            tableAlias = "sil"
    )
    @WebAdvanceQuery(handler = SampleInitialLedgerQueryHandler.class)
    public ApiResult<PagingVO<SampleInitialLedgerDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleInitialLedgerDTO.PagingParamDTO> dto) {
        return success(sampleInitialLedgerService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleInitialLedgerDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = sampleInitialLedgerService.addAndSubmit(dto);
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
            menuCode = "wms:sampleInitialLedger:updateAndSubmit",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleInitialLedgerDTO.UpdateDTO dto) {
        sampleInitialLedgerService.updateAndSubmit(dto);
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
            menuCode = "wms:sampleInitialLedger:submit",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品期初台账提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleInitialLedgerEntity> list = sampleInitialLedgerService.lambdaQuery().in(SampleInitialLedgerEntity::getId, ids).list();
		Map<String, SampleInitialLedgerEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleInitialLedgerEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleInitialLedgerService.submit(id);
            }catch (Exception e){
                log.error("样品期初台账 提交审核失败",e);
                SampleInitialLedgerEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品期初台账不存在, 提交失败");
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
            menuCode = "wms:sampleInitialLedger:approve",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品期初台账审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleInitialLedgerEntity> list = sampleInitialLedgerService.lambdaQuery().in(SampleInitialLedgerEntity::getId, ids).list();
		Map<String, SampleInitialLedgerEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleInitialLedgerEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleInitialLedgerService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("样品期初台账审核失败",e);
                SampleInitialLedgerEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品期初台账不存在, 审核失败");
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
    * 删除
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleInitialLedger:delete",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品期初台账删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleInitialLedgerEntity> list = sampleInitialLedgerService.lambdaQuery().in(SampleInitialLedgerEntity::getId, ids).list();
		Map<String, SampleInitialLedgerEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleInitialLedgerEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleInitialLedgerService.delete(id);
            }catch (Exception e){
                log.error("样品期初台账删除失败",e);
                SampleInitialLedgerEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品期初台账不存在, 删除失败");
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
            menuCode = "wms:sampleInitialLedger:invalid",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品期初台账作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SampleInitialLedgerEntity> list = sampleInitialLedgerService.lambdaQuery().in(SampleInitialLedgerEntity::getId, ids).list();
		Map<String, SampleInitialLedgerEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleInitialLedgerEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = sampleInitialLedgerService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("样品期初台账作废失败",e);
                SampleInitialLedgerEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "样品期初台账不存在, 作废失败");
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
            menuCode = "wms:sampleInitialLedger:cancelProcess",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品期初台账撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SampleInitialLedgerEntity> list = sampleInitialLedgerService.lambdaQuery().in(SampleInitialLedgerEntity::getId, ids).list();
        Map<String, SampleInitialLedgerEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleInitialLedgerEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleInitialLedgerService.cancelProcess(id);
            }catch (Exception e){
                log.error("样品期初台账撤回流程失败",e);
                SampleInitialLedgerEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品期初台账不存在, 撤回流程失败");
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
    * @return ApiResult<SampleInitialLedgerDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleInitialLedger:view",
            serviceClass = SampleInitialLedgerService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SampleInitialLedgerDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleInitialLedgerService.view(id));
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
            menuCode = "wms:sampleInitialLedger:export"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品期初台账导出Excel数据")
    public void exportList(@RequestBody @Validated SampleInitialLedgerDTO.ExportDTO dto, HttpServletResponse response) {
        sampleInitialLedgerService.exportList(dto, response);
    }


    /**
    * 异步导入
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入样品期初台账")
    @PostMapping("/importFile")
    public ApiResult<Object> importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = sampleInitialLedgerService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-08-21
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品期初台账导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        sampleInitialLedgerService.downloadTemplate(response);
        return success();
    }


}
