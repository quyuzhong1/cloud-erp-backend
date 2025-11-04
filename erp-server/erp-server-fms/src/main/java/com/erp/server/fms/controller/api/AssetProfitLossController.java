package com.erp.server.fms.controller.api;


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
import com.erp.server.fms.service.AssetProfitLossService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetProfitLossDTO;
import com.erp.server.fms.handler.AssetProfitLossQueryHandler;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.common.business.enums.FileTaskEventEnum;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.fms.entity.AssetProfitLossEntity;

/**
 * 盘盈盘亏单主表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("盘盈盘亏单主表")
@RequestMapping("/assetProfitLoss")
public class AssetProfitLossController extends BaseController {

    @Resource
    private AssetProfitLossService assetProfitLossService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

//    /**
//    * 新增
//    * @author wuht
//    * @date:  2025-10-11
//    * @param dto
//    * @return ApiResult<String>
//    */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "盘盈盘亏单主表新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetProfitLossDTO.AddDTO dto) {
//        return success(assetProfitLossService.add(dto));
//    }
//
//    /**
//    * 修改
//    * @author wuht
//    * @date:  2025-10-11
//    * @param dto
//    * @return ApiResult
//    */
//    @PostMapping("/update")
//    @LogAction(value = LogActionEnum.UPDATE, desc = "盘盈盘亏单主表修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "fms:assetProfitLoss:update",
//        serviceClass = AssetProfitLossService.class,
//        keyIdName = "id")
//    public ApiResult<?> update(@RequestBody @Validated AssetProfitLossDTO.UpdateDTO dto) {
//        assetProfitLossService.update(dto);
//        return success();
//    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetProfitLoss:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetProfitLossDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetProfitLossService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return ApiResult<PagingVO<AssetProfitLossDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetProfitLoss:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = AssetProfitLossQueryHandler.class)
    public ApiResult<PagingVO<AssetProfitLossDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetProfitLossDTO.PagingParamDTO> dto) {
        return success(assetProfitLossService.paging(dto));
    }
//
//    /**
//    * 新增并提交审核
//    * @author wuht
//    * @date:  2025-10-11
//    * @param dto
//    * @return ApiResult<Void>
//    */
//    @PostMapping("/addAndSubmit")
//    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetProfitLossDTO.AddDTO dto) {
//        BaseResultDTO.AddDTO result = assetProfitLossService.addAndSubmit(dto);
//        return success(result);
//    }

//    /**
//    * 修改并提交审核
//    * @author wuht
//    * @date:  2025-10-11
//    * @param dto
//    * @return ApiResult<Void>
//    */
//    @PostMapping("/updateAndSubmit")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "fms:assetProfitLoss:updateAndSubmit",
//            serviceClass = AssetProfitLossService.class,
//            keyIdName = "id")
//    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetProfitLossDTO.UpdateDTO dto) {
//        assetProfitLossService.updateAndSubmit(dto);
//        return success();
//    }

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
            menuCode = "fms:assetProfitLoss:submit",
            serviceClass = AssetProfitLossService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "盘盈盘亏单主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetProfitLossEntity> list = assetProfitLossService.lambdaQuery().in(AssetProfitLossEntity::getId, ids).list();
		Map<String, AssetProfitLossEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetProfitLossEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetProfitLossService.submit(id);
            }catch (Exception e){
                log.error("盘盈盘亏单主单 提交审核失败",e);
                AssetProfitLossEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘盈盘亏单主单不存在, 提交失败");
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
            menuCode = "fms:assetProfitLoss:approve",
            serviceClass = AssetProfitLossService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "盘盈盘亏单主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetProfitLossEntity> list = assetProfitLossService.lambdaQuery().in(AssetProfitLossEntity::getId, ids).list();
		Map<String, AssetProfitLossEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetProfitLossEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetProfitLossService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("盘盈盘亏单主单审核失败",e);
                AssetProfitLossEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "盘盈盘亏单主单不存在, 审核失败");
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
            menuCode = "fms:assetProfitLoss:disApprove",
            serviceClass = AssetProfitLossService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "盘盈盘亏单主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetProfitLossEntity> list = assetProfitLossService.lambdaQuery().in(AssetProfitLossEntity::getId, ids).list();
		Map<String, AssetProfitLossEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetProfitLossEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetProfitLossService.disApprove(id);
            }catch (Exception e){
                log.error("盘盈盘亏单主单反审核失败",e);
                AssetProfitLossEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "盘盈盘亏单主单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


//    /**
//    * 删除
//    * @author wuht
//    * @date:  2025-10-11
//    * @param dto
//    * @return ApiResult<List<BatchResultDTO>>
//    */
//    @PostMapping("/delete")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "fms:assetProfitLoss:delete",
//            serviceClass = AssetProfitLossService.class,
//            keyIdName = "ids")
//    @LogAction(value = LogActionEnum.DELETE, desc = "盘盈盘亏单主表删除")
//    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
//        List<String> ids = dto.getIds();
//		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
//		// TODO 数据查询放入外层，处理结果统一更新或单条更新
//		List<AssetProfitLossEntity> list = assetProfitLossService.lambdaQuery().in(AssetProfitLossEntity::getId, ids).list();
//		Map<String, AssetProfitLossEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetProfitLossEntity::getId, w -> w));
//        for (String id : dto.getIds()) {
//            BatchResultDTO deleteResult;
//            try {
//                deleteResult = assetProfitLossService.delete(id);
//            }catch (Exception e){
//                log.error("盘盈盘亏单主单删除失败",e);
//                AssetProfitLossEntity entity = idEntityMap.get(id);
//                if (ObjectUtil.isEmpty(entity)) {
//                    deleteResult = BatchResultDTO.fail(id, id, "盘盈盘亏单主单不存在, 删除失败");
//                    resultDTOS.add(deleteResult);
//                    continue;
//                }
//                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
//            }
//            resultDTOS.add(deleteResult);
//        }
//        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
//    }
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
            menuCode = "fms:assetProfitLoss:invalid",
            serviceClass = AssetProfitLossService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "盘盈盘亏单主表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetProfitLossEntity> list = assetProfitLossService.lambdaQuery().in(AssetProfitLossEntity::getId, ids).list();
		Map<String, AssetProfitLossEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetProfitLossEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetProfitLossService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("盘盈盘亏单主单作废失败",e);
                AssetProfitLossEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "盘盈盘亏单主单不存在, 作废失败");
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
            menuCode = "fms:assetProfitLoss:cancelProcess",
            serviceClass = AssetProfitLossService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "盘盈盘亏单主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<AssetProfitLossEntity> list = assetProfitLossService.lambdaQuery().in(AssetProfitLossEntity::getId, ids).list();
        Map<String, AssetProfitLossEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetProfitLossEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetProfitLossService.cancelProcess(id);
            }catch (Exception e){
                log.error("盘盈盘亏单主单撤回流程失败",e);
                AssetProfitLossEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "盘盈盘亏单主单不存在, 撤回流程失败");
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
    * @return ApiResult<AssetProfitLossDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetProfitLoss:view",
            serviceClass = AssetProfitLossService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetProfitLossDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetProfitLossService.view(id));
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
            menuCode = "fms:assetProfitLoss:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "盘盈盘亏单主表导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated AssetProfitLossDTO.ExportDTO dto, HttpServletResponse response) {
        // 异步导出任务
        downloadTaskFeign.saveDownloadTask("盘盈盘亏单导出", FileTaskEventEnum.EXPORT_FMS_ASSET_PROFIT_LOSS.getCode(), dto);
        return success(true);
    }

    /**
     * 获取盘盈盘亏单下推到资产卡片前的列表数据
     * @author wuht
     * @date:  2025-11-04
     * @param id 盘盈盘亏单主键ID
     * @return ApiResult<List<AssetProfitLossDTO.PushToCardListDTO>>
     */
    @GetMapping("/getPushToCardList")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "fms:assetProfitLoss:view",
//            serviceClass = AssetProfitLossService.class,
//            keyIdName = "id")
    public ApiResult<List<AssetProfitLossDTO.PushToCardListDTO>> getPushToCardList(@RequestParam("id") String id) {
        return success(assetProfitLossService.getPushToCardList(id));
    }



}
