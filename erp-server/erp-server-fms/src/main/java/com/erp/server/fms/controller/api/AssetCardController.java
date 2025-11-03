package com.erp.server.fms.controller.api;


import lombok.extern.slf4j.Slf4j;

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
import com.erp.server.fms.service.AssetCardService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetCardDTO;
import com.erp.server.fms.handler.AssetCardQueryHandler;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.fms.entity.AssetCardEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.common.business.enums.FileTaskEventEnum;

/**
 * 资产卡片主表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产卡片主表")
@RequestMapping("/assetCard")
public class AssetCardController extends BaseController {

    @Resource
    private AssetCardService assetCardService;
    
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
    @LogAction(value = LogActionEnum.INSERT, desc = "资产卡片主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetCardDTO.AddDTO dto) {
        return success(assetCardService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产卡片主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetCard:update",
        serviceClass = AssetCardService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetCardDTO.UpdateDTO dto) {
        assetCardService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetCard:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetCardDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetCardService.tabList(dto));
    }

    /**
    * 列表查询 fms:assetCard:paging
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return ApiResult<PagingVO<AssetCardDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetCard:paging",
            tableAlias = "ac"
    )
    @WebAdvanceQuery(handler = AssetCardQueryHandler.class)
    public ApiResult<PagingVO<AssetCardDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetCardDTO.PagingParamDTO> dto) {
        return success(assetCardService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetCardDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetCardService.addAndSubmit(dto);
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
            menuCode = "fms:assetCard:updateAndSubmit",
            serviceClass = AssetCardService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetCardDTO.UpdateDTO dto) {
        assetCardService.updateAndSubmit(dto);
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
            menuCode = "fms:assetCard:submit",
            serviceClass = AssetCardService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "资产卡片主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetCardEntity> list = assetCardService.lambdaQuery().in(AssetCardEntity::getId, ids).list();
		Map<String, AssetCardEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetCardEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetCardService.submit(id);
            }catch (Exception e){
                log.error("资产卡片主单 提交审核失败",e);
                AssetCardEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "资产卡片主单不存在, 提交失败");
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
            menuCode = "fms:assetCard:approve",
            serviceClass = AssetCardService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产卡片主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetCardEntity> list = assetCardService.lambdaQuery().in(AssetCardEntity::getId, ids).list();
		Map<String, AssetCardEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetCardEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetCardService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("资产卡片主单审核失败",e);
                AssetCardEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "资产卡片主单不存在, 审核失败");
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
            menuCode = "fms:assetCard:disApprove",
            serviceClass = AssetCardService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "资产卡片主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetCardEntity> list = assetCardService.lambdaQuery().in(AssetCardEntity::getId, ids).list();
		Map<String, AssetCardEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetCardEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetCardService.disApprove(id);
            }catch (Exception e){
                log.error("资产卡片主单反审核失败",e);
                AssetCardEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "资产卡片主单不存在, 反审核失败");
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
            menuCode = "fms:assetCard:delete",
            serviceClass = AssetCardService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "资产卡片主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetCardEntity> list = assetCardService.lambdaQuery().in(AssetCardEntity::getId, ids).list();
		Map<String, AssetCardEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetCardEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetCardService.delete(id);
            }catch (Exception e){
                log.error("资产卡片主单删除失败",e);
                AssetCardEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "资产卡片主单不存在, 删除失败");
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
            menuCode = "fms:assetCard:invalid",
            serviceClass = AssetCardService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "资产卡片主表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetCardEntity> list = assetCardService.lambdaQuery().in(AssetCardEntity::getId, ids).list();
		Map<String, AssetCardEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetCardEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetCardService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("资产卡片主单作废失败",e);
                AssetCardEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "资产卡片主单不存在, 作废失败");
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
            menuCode = "fms:assetCard:cancelProcess",
            serviceClass = AssetCardService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "资产卡片主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<AssetCardEntity> list = assetCardService.lambdaQuery().in(AssetCardEntity::getId, ids).list();
        Map<String, AssetCardEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetCardEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetCardService.cancelProcess(id);
            }catch (Exception e){
                log.error("资产卡片主单撤回流程失败",e);
                AssetCardEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "资产卡片主单不存在, 撤回流程失败");
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
    * @return ApiResult<AssetCardDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetCard:view",
            serviceClass = AssetCardService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetCardDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetCardService.view(id));
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
            menuCode = "fms:assetCard:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "资产卡片主表导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated AssetCardDTO.ExportDTO dto, HttpServletResponse response) {
        // 异步导出任务
        downloadTaskFeign.saveDownloadTask("资产卡片导出", FileTaskEventEnum.EXPORT_FMS_ASSET_CARD.getCode(), dto);
        return success(true);
    }

    /**
    * 导入Excel
    * @author wuht
    * @date: 2025-01-10
    * @param dto
    * @return
    */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "资产卡片主表导入Excel")
    public ApiResult<Boolean> importFile(@RequestBody @Validated BaseDTO.ImportDTO dto) {
        return success(assetCardService.importFile(dto));
    }

    /**
    * 下载导入模板
    * @author wuht
    * @date: 2025-11-03
    * @param response
    * @return ApiResult<Object>
    */
    @GetMapping("/downloadTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载资产卡片导入模板")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        assetCardService.downloadTemplate(response);
        return success();
    }

    /**
    * 获取已审核资产卡片列表（用于盘点方案）
    * @author wuht
    * @date: 2025-10-24
    * @param dto
    * @return
    */
    @PostMapping("/getApprovedCardList")
    public ApiResult<List<AssetCardDTO.ApprovedCardDTO>> getApprovedCardList(@RequestBody AssetCardDTO.QueryApprovedDTO dto) {
        return success(assetCardService.getApprovedCardList(dto));
    }

}
