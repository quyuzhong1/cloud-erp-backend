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
import com.erp.server.fms.service.AssetStocktakingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetStocktakingDTO;
import com.erp.server.fms.handler.AssetStocktakingQueryHandler;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.fms.entity.AssetStocktakingEntity;

/**
 * 资产盘点表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产盘点表")
@RequestMapping("/assetStocktaking")
public class AssetStocktakingController extends BaseController {

    @Resource
    private AssetStocktakingService assetStocktakingService;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产盘点表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetStocktakingDTO.AddDTO dto) {
        return success(assetStocktakingService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产盘点表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetStocktaking:update",
        serviceClass = AssetStocktakingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetStocktakingDTO.UpdateDTO dto) {
        assetStocktakingService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetStocktaking:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetStocktakingDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetStocktakingService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return ApiResult<PagingVO<AssetStocktakingDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetStocktaking:paging",
            tableAlias = "ast"
    )
    @WebAdvanceQuery(handler = AssetStocktakingQueryHandler.class)
    public ApiResult<PagingVO<AssetStocktakingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetStocktakingDTO.PagingParamDTO> dto) {
        return success(assetStocktakingService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetStocktakingDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetStocktakingService.addAndSubmit(dto);
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
            menuCode = "fms:assetStocktaking:updateAndSubmit",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetStocktakingDTO.UpdateDTO dto) {
        assetStocktakingService.updateAndSubmit(dto);
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
            menuCode = "fms:assetStocktaking:submit",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "资产盘点表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetStocktakingEntity> list = assetStocktakingService.lambdaQuery().in(AssetStocktakingEntity::getId, ids).list();
		Map<String, AssetStocktakingEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetStocktakingEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetStocktakingService.submit(id);
            }catch (Exception e){
                log.error("资产盘点单 提交审核失败",e);
                AssetStocktakingEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "资产盘点单不存在, 提交失败");
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
            menuCode = "fms:assetStocktaking:approve",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产盘点表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetStocktakingEntity> list = assetStocktakingService.lambdaQuery().in(AssetStocktakingEntity::getId, ids).list();
		Map<String, AssetStocktakingEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetStocktakingEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetStocktakingService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("资产盘点单审核失败",e);
                AssetStocktakingEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "资产盘点单不存在, 审核失败");
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
            menuCode = "fms:assetStocktaking:disApprove",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "资产盘点表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetStocktakingEntity> list = assetStocktakingService.lambdaQuery().in(AssetStocktakingEntity::getId, ids).list();
		Map<String, AssetStocktakingEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetStocktakingEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetStocktakingService.disApprove(id);
            }catch (Exception e){
                log.error("资产盘点单反审核失败",e);
                AssetStocktakingEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "资产盘点单不存在, 反审核失败");
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
            menuCode = "fms:assetStocktaking:delete",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "资产盘点表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetStocktakingEntity> list = assetStocktakingService.lambdaQuery().in(AssetStocktakingEntity::getId, ids).list();
		Map<String, AssetStocktakingEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetStocktakingEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetStocktakingService.delete(id);
            }catch (Exception e){
                log.error("资产盘点单删除失败",e);
                AssetStocktakingEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "资产盘点单不存在, 删除失败");
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
            menuCode = "fms:assetStocktaking:invalid",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "资产盘点表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetStocktakingEntity> list = assetStocktakingService.lambdaQuery().in(AssetStocktakingEntity::getId, ids).list();
		Map<String, AssetStocktakingEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetStocktakingEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetStocktakingService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("资产盘点单作废失败",e);
                AssetStocktakingEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "资产盘点单不存在, 作废失败");
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
            menuCode = "fms:assetStocktaking:cancelProcess",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "资产盘点表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<AssetStocktakingEntity> list = assetStocktakingService.lambdaQuery().in(AssetStocktakingEntity::getId, ids).list();
        Map<String, AssetStocktakingEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetStocktakingEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetStocktakingService.cancelProcess(id);
            }catch (Exception e){
                log.error("资产盘点单撤回流程失败",e);
                AssetStocktakingEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "资产盘点单不存在, 撤回流程失败");
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
    * @return ApiResult<AssetStocktakingDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:assetStocktaking:view",
            serviceClass = AssetStocktakingService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetStocktakingDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetStocktakingService.view(id));
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
            menuCode = "fms:assetStocktaking:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "资产盘点表导出Excel数据")
    public void exportList(@RequestBody @Validated AssetStocktakingDTO.ExportDTO dto, HttpServletResponse response) {
        assetStocktakingService.exportList(dto, response);
    }

    /**
    * 生成卡片编码
    * @author wuht
    * @date:  2025-10-31
    * @return ApiResult<String>
    */
    @GetMapping("/generateCardCode")
    public ApiResult<String> generateCardCode() {
        String code = assetStocktakingService.generateCardCode();
        return success(code);
    }

    /**
    * 资产盘点表下拉列表
    * @author wuht
    * @date: 2025-11-04
    * @param keyword 关键字（支持盘点单号、来源单号模糊查询）
    * @return ApiResult<List<AssetStocktakingDTO.DropDownDTO>>
    */
    @GetMapping("/dropDownList")
    public ApiResult<List<AssetStocktakingDTO.DropDownDTO>> dropDownList(@RequestParam(value = "keyword", required = false) String keyword) {
        return success(assetStocktakingService.dropDownList(keyword));
    }


}
