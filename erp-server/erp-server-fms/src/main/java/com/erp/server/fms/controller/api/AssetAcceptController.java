package com.erp.server.fms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.erp.server.fms.handler.AssetAcceptQueryHandler;
import com.erp.server.fms.service.AssetAcceptService;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.common.business.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资产验收表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产验收表")
@RequestMapping("/assetAccept")
public class AssetAcceptController extends BaseController {

    @Resource
    private AssetAcceptService assetAcceptService;
    
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产验收表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetAcceptDTO.AddDTO dto) {
        return success(assetAcceptService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产验收表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetAccept:update",
        serviceClass = AssetAcceptService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetAcceptDTO.UpdateDTO dto) {
        assetAcceptService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetAcceptDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetAcceptService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return ApiResult<PagingVO<AssetAcceptDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = AssetAcceptQueryHandler.class)
    public ApiResult<PagingVO<AssetAcceptDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetAcceptDTO.PagingParamDTO> dto) {
        return success(assetAcceptService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetAcceptDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetAcceptService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:updateAndSubmit",
            serviceClass = AssetAcceptService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetAcceptDTO.UpdateDTO dto) {
        assetAcceptService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:submit",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "资产验收表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
		Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetAcceptService.submit(id);
            }catch (Exception e){
                log.error("资产验收单 提交审核失败",e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "资产验收单不存在, 提交失败");
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
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:approve",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产验收表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
		Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetAcceptService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("资产验收单审核失败",e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "资产验收单不存在, 审核失败");
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
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:disApprove",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "资产验收表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
		Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetAcceptService.disApprove(id);
            }catch (Exception e){
                log.error("资产验收单反审核失败",e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "资产验收单不存在, 反审核失败");
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
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:delete",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "资产验收表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
		Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetAcceptService.delete(id);
            }catch (Exception e){
                log.error("资产验收单删除失败",e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "资产验收单不存在, 删除失败");
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
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:invalid",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "资产验收表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
		Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetAcceptService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("资产验收单作废失败",e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "资产验收单不存在, 作废失败");
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
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:cancelProcess",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "资产验收表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
        Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetAcceptService.cancelProcess(id);
            }catch (Exception e){
                log.error("资产验收单撤回流程失败",e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "资产验收单不存在, 撤回流程失败");
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
    * @author wuht
    * @date:  2025-10-11
    * @param id
    * @return ApiResult<AssetAcceptDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:view",
            serviceClass = AssetAcceptService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetAcceptDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetAcceptService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "资产验收表导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated AssetAcceptDTO.ExportDTO dto, HttpServletResponse response) {
        // 异步导出任务
        downloadTaskFeign.saveDownloadTask("资产验收表导出", FileTaskEventEnum.EXPORT_FMS_ASSET_ACCEPT_REPORT.getCode(), dto);
        return success(true);
    }

    /**
    * 转资产卡片
    * @author wuht
    * @date:  2025-10-11
    * @param dto 包含明细ID列表
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/transferToAssetCard")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:transferToAssetCard",
            serviceClass = AssetAcceptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "资产验收表转资产卡片")
    public ApiResult<List<BatchResultDTO>> transferToAssetCard(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AssetAcceptEntity> list = assetAcceptService.lambdaQuery().in(AssetAcceptEntity::getId, ids).list();
        Map<String, AssetAcceptEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetAcceptEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO transferResult;
            try {
                transferResult = assetAcceptService.transferToAssetCard(id);
            } catch (Exception e) {
                log.error("资产验收单转资产卡片失败", e);
                AssetAcceptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    transferResult = BatchResultDTO.fail(id, id, "资产验收单不存在, 转资产卡片失败");
                    resultDTOS.add(transferResult);
                    continue;
                }
                transferResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(transferResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


}
