package com.erp.server.fms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.fms.handler.AssetLocationQueryHandler;
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
import com.erp.server.fms.service.AssetLocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetLocationDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.fms.entity.AssetLocationEntity;

/**
 * 资产位置表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产位置表")
@RequestMapping("/assetLocation")
public class AssetLocationController extends BaseController {

    @Resource
    private AssetLocationService assetLocationService;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产位置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetLocationDTO.AddDTO dto) {
        return success(assetLocationService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产位置表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetLocation:update",
        serviceClass = AssetLocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetLocationDTO.UpdateDTO dto) {
        assetLocationService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetLocation:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetLocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetLocationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return ApiResult<PagingVO<AssetLocationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetLocation:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = AssetLocationQueryHandler.class)
    public ApiResult<PagingVO<AssetLocationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetLocationDTO.PagingParamDTO> dto) {
        return success(assetLocationService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetLocationDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetLocationService.addAndSubmit(dto);
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
            menuCode = "fms:assetLocation:updateAndSubmit",
            serviceClass = AssetLocationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetLocationDTO.UpdateDTO dto) {
        assetLocationService.updateAndSubmit(dto);
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
            menuCode = "fms:assetLocation:submit",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "资产位置表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetLocationEntity> list = assetLocationService.lambdaQuery().in(AssetLocationEntity::getId, ids).list();
		Map<String, AssetLocationEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetLocationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetLocationService.submit(id);
            }catch (Exception e){
                log.error("资产位置单 提交审核失败",e);
                AssetLocationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "资产位置单不存在, 提交失败");
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
            menuCode = "fms:assetLocation:approve",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产位置表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetLocationEntity> list = assetLocationService.lambdaQuery().in(AssetLocationEntity::getId, ids).list();
		Map<String, AssetLocationEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetLocationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetLocationService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("资产位置单审核失败",e);
                AssetLocationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "资产位置单不存在, 审核失败");
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
            menuCode = "fms:assetLocation:disApprove",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "资产位置表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetLocationEntity> list = assetLocationService.lambdaQuery().in(AssetLocationEntity::getId, ids).list();
		Map<String, AssetLocationEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetLocationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetLocationService.disApprove(id);
            }catch (Exception e){
                log.error("资产位置单反审核失败",e);
                AssetLocationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "资产位置单不存在, 反审核失败");
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
            menuCode = "fms:assetLocation:delete",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "资产位置表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetLocationEntity> list = assetLocationService.lambdaQuery().in(AssetLocationEntity::getId, ids).list();
		Map<String, AssetLocationEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetLocationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetLocationService.delete(id);
            }catch (Exception e){
                log.error("资产位置单删除失败",e);
                AssetLocationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "资产位置单不存在, 删除失败");
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
            menuCode = "fms:assetLocation:invalid",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "资产位置表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetLocationEntity> list = assetLocationService.lambdaQuery().in(AssetLocationEntity::getId, ids).list();
		Map<String, AssetLocationEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetLocationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetLocationService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("资产位置单作废失败",e);
                AssetLocationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "资产位置单不存在, 作废失败");
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
            menuCode = "fms:assetLocation:cancelProcess",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "资产位置表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<AssetLocationEntity> list = assetLocationService.lambdaQuery().in(AssetLocationEntity::getId, ids).list();
        Map<String, AssetLocationEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetLocationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetLocationService.cancelProcess(id);
            }catch (Exception e){
                log.error("资产位置单撤回流程失败",e);
                AssetLocationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "资产位置单不存在, 撤回流程失败");
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
    * @return ApiResult<AssetLocationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetLocation:view",
            serviceClass = AssetLocationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetLocationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetLocationService.view(id));
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
            menuCode = "fms:assetLocation:export",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = AssetLocationQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "资产位置表导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated AssetLocationDTO.ExportDTO dto, HttpServletResponse response) {
        assetLocationService.exportList(dto, response);
        return success(true);
    }

    /**
    * 批量启用/禁用
    * @author wuht
    * @date:  2025-10-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetLocation:updateStatus",
            serviceClass = AssetLocationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        assetLocationService.updateStatus(dto);
        return success();
    }

    /**
     * 下载模板
     * @author wuht
     * @date: 2025-10-13
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载资产位置导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        assetLocationService.downloadTemplate(response);
        return success();
    }

    /**
     * 异步导入
     * @author wuht
     * @date: 2025-10-13
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/importExcel")
    public ApiResult<Object> importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean flag = assetLocationService.importExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 获取资产位置下拉列表
     * @author wuhaotian
     * @date: 2025-10-21
     * @param keyword 关键字（支持编码和地址模糊查询）
     * @return ApiResult<List<AssetLocationDTO.DropDownDTO>>
     */
    @GetMapping("/drop/down/list")
    public ApiResult<List<AssetLocationDTO.DropDownDTO>> dropDownList(@RequestParam(value = "keyword", required = false) String keyword) {
        return success(assetLocationService.dropDownList(keyword));
    }

}
