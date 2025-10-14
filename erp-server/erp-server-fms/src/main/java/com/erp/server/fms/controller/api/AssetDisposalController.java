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
import com.erp.server.fms.service.AssetDisposalService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetDisposalDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.fms.entity.AssetDisposalEntity;

/**
 * 资产处置单主表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产处置单主表")
@RequestMapping("/assetDisposal")
public class AssetDisposalController extends BaseController {

    @Resource
    private AssetDisposalService assetDisposalService;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产处置单主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetDisposalDTO.AddDTO dto) {
        return success(assetDisposalService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产处置单主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetDisposal:update",
        serviceClass = AssetDisposalService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetDisposalDTO.UpdateDTO dto) {
        assetDisposalService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetDisposal:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetDisposalDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetDisposalService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return ApiResult<PagingVO<AssetDisposalDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetDisposal:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<AssetDisposalDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetDisposalDTO.PagingParamDTO> dto) {
        return success(assetDisposalService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetDisposalDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetDisposalService.addAndSubmit(dto);
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
            menuCode = "fms:assetDisposal:updateAndSubmit",
            serviceClass = AssetDisposalService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetDisposalDTO.UpdateDTO dto) {
        assetDisposalService.updateAndSubmit(dto);
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
            menuCode = "fms:assetDisposal:submit",
            serviceClass = AssetDisposalService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "资产处置单主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetDisposalEntity> list = assetDisposalService.lambdaQuery().in(AssetDisposalEntity::getId, ids).list();
		Map<String, AssetDisposalEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetDisposalEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetDisposalService.submit(id);
            }catch (Exception e){
                log.error("资产处置单主单 提交审核失败",e);
                AssetDisposalEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "资产处置单主单不存在, 提交失败");
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
            menuCode = "fms:assetDisposal:approve",
            serviceClass = AssetDisposalService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产处置单主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetDisposalEntity> list = assetDisposalService.lambdaQuery().in(AssetDisposalEntity::getId, ids).list();
		Map<String, AssetDisposalEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetDisposalEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetDisposalService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("资产处置单主单审核失败",e);
                AssetDisposalEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "资产处置单主单不存在, 审核失败");
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
            menuCode = "fms:assetDisposal:disApprove",
            serviceClass = AssetDisposalService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "资产处置单主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetDisposalEntity> list = assetDisposalService.lambdaQuery().in(AssetDisposalEntity::getId, ids).list();
		Map<String, AssetDisposalEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetDisposalEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetDisposalService.disApprove(id);
            }catch (Exception e){
                log.error("资产处置单主单反审核失败",e);
                AssetDisposalEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "资产处置单主单不存在, 反审核失败");
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
            menuCode = "fms:assetDisposal:delete",
            serviceClass = AssetDisposalService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "资产处置单主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetDisposalEntity> list = assetDisposalService.lambdaQuery().in(AssetDisposalEntity::getId, ids).list();
		Map<String, AssetDisposalEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetDisposalEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetDisposalService.delete(id);
            }catch (Exception e){
                log.error("资产处置单主单删除失败",e);
                AssetDisposalEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "资产处置单主单不存在, 删除失败");
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
            menuCode = "fms:assetDisposal:invalid",
            serviceClass = AssetDisposalService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "资产处置单主表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetDisposalEntity> list = assetDisposalService.lambdaQuery().in(AssetDisposalEntity::getId, ids).list();
		Map<String, AssetDisposalEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetDisposalEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetDisposalService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("资产处置单主单作废失败",e);
                AssetDisposalEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "资产处置单主单不存在, 作废失败");
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
            menuCode = "fms:assetDisposal:cancelProcess",
            serviceClass = AssetDisposalService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "资产处置单主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<AssetDisposalEntity> list = assetDisposalService.lambdaQuery().in(AssetDisposalEntity::getId, ids).list();
        Map<String, AssetDisposalEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetDisposalEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetDisposalService.cancelProcess(id);
            }catch (Exception e){
                log.error("资产处置单主单撤回流程失败",e);
                AssetDisposalEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "资产处置单主单不存在, 撤回流程失败");
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
    * @return ApiResult<AssetDisposalDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetDisposal:view",
            serviceClass = AssetDisposalService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetDisposalDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetDisposalService.view(id));
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
            menuCode = "fms:assetDisposal:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "资产处置单主表导出Excel数据")
    public void exportList(@RequestBody @Validated AssetDisposalDTO.ExportDTO dto, HttpServletResponse response) {
        assetDisposalService.exportList(dto, response);
    }


}
