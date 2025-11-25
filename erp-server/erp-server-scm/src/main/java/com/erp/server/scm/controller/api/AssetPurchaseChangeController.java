package com.erp.server.scm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.scm.query.AssetPurchaseChangeQueryHandler;
import com.erp.server.scm.query.AssetPurchaseOrderQueryHandler;
import com.erp.server.scm.service.AssetPurchaseChangeService;
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
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.AssetPurchaseChangeDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.scm.entity.AssetPurchaseChangeEntity;

/**
 * 
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@RestController
@LogSystemModule("资产采购变更单")
@RequestMapping("/assetPurchaseChange")
public class AssetPurchaseChangeController extends BaseController {

    @Resource
    private AssetPurchaseChangeService assetPurchaseChangeService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增资产采购变更单")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody AssetPurchaseChangeDTO.AddDTO dto) {
        return success(assetPurchaseChangeService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改资产采购变更单")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:assetPurchaseChange:update",
        serviceClass = AssetPurchaseChangeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetPurchaseChangeDTO.UpdateDTO dto) {
        assetPurchaseChangeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetPurchaseChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetPurchaseChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return ApiResult<PagingVO<AssetPurchaseChangeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = AssetPurchaseChangeQueryHandler.class)
    public ApiResult<PagingVO<AssetPurchaseChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetPurchaseChangeDTO.PagingParamDTO> dto) {
        return success(assetPurchaseChangeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetPurchaseChangeDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetPurchaseChangeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:updateAndSubmit",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetPurchaseChangeDTO.UpdateDTO dto) {
        assetPurchaseChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:submit",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
		Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetPurchaseChangeService.submit(id);
            }catch (Exception e){
                log.error(" 提交审核失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "不存在, 提交失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:approve",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
		Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetPurchaseChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("审核失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "不存在, 审核失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:disApprove",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
		Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetPurchaseChangeService.disApprove(id);
            }catch (Exception e){
                log.error("反审核失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "不存在, 反审核失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:delete",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
		Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetPurchaseChangeService.delete(id);
            }catch (Exception e){
                log.error("删除失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "不存在, 删除失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:invalid",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
		Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetPurchaseChangeService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("作废失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "不存在, 作废失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:cancelProcess",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
        Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetPurchaseChangeService.cancelProcess(id);
            }catch (Exception e){
                log.error("撤回流程失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "不存在, 撤回流程失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param id
    * @return ApiResult<AssetPurchaseChangeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:view",
            serviceClass = AssetPurchaseChangeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetPurchaseChangeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetPurchaseChangeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseChange:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public ApiResult<Object> exportList(@RequestBody @Validated AssetPurchaseChangeDTO.ExportDTO dto, HttpServletResponse response) {
        assetPurchaseChangeService.exportList(dto, response);
        return success();
    }


}
