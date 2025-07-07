package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.wms.query.VirtualAdjustQueryHandler;
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
import com.erp.server.wms.service.VirtualAdjustService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualAdjustDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.VirtualAdjustEntity;

/**
 * 虚拟仓调整单主表
 *
 * @author zdy
 * @since 2025-06-09
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓调整单主表")
@RequestMapping("/virtualAdjust")
public class VirtualAdjustController extends BaseController {

    @Resource
    private VirtualAdjustService virtualAdjustService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓调整单主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualAdjustDTO.AddDTO dto) {
        BaseResultDTO.AddDTO add = virtualAdjustService.add(dto);
        return success(add);
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓调整单主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualAdjust:update",
        serviceClass = VirtualAdjustService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualAdjustDTO.UpdateDTO dto) {
        virtualAdjustService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:paging",
            tableAlias = "va"
    )
    public ApiResult<List<VirtualAdjustDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(virtualAdjustService.tabList(dto));
    }

    /**
    * 列表查询
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return ApiResult<PagingVO<VirtualAdjustDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:paging",
            tableAlias = "va"
    )
    @WebAdvanceQuery(handler = VirtualAdjustQueryHandler.class)
    public ApiResult<PagingVO<VirtualAdjustDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualAdjustDTO.PagingParamDTO> dto) {
        return success(virtualAdjustService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated VirtualAdjustDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = virtualAdjustService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:updateAndSubmit",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated VirtualAdjustDTO.UpdateDTO dto) {
        virtualAdjustService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:submit",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "虚拟仓调整单主表提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<VirtualAdjustEntity> list = virtualAdjustService.lambdaQuery().in(VirtualAdjustEntity::getId, ids).list();
		Map<String, VirtualAdjustEntity> idEntityMap = list.stream().collect(Collectors.toMap(VirtualAdjustEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = virtualAdjustService.submit(id);
            }catch (Exception e){
                log.error("虚拟仓调整单主单 提交审核失败",e);
                VirtualAdjustEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "虚拟仓调整单主单不存在, 提交失败");
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
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:approve",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "虚拟仓调整单主表审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<VirtualAdjustEntity> list = virtualAdjustService.lambdaQuery().in(VirtualAdjustEntity::getId, ids).list();
		Map<String, VirtualAdjustEntity> idEntityMap = list.stream().collect(Collectors.toMap(VirtualAdjustEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = virtualAdjustService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("虚拟仓调整单主单审核失败",e);
                VirtualAdjustEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "虚拟仓调整单主单不存在, 审核失败");
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
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:disApprove",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "虚拟仓调整单主表反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		//数据查询放入外层，处理结果统一更新或单条更新
		List<VirtualAdjustEntity> list = virtualAdjustService.lambdaQuery().in(VirtualAdjustEntity::getId, ids).list();
		Map<String, VirtualAdjustEntity> idEntityMap = list.stream().collect(Collectors.toMap(VirtualAdjustEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = virtualAdjustService.disApprove(id);
            }catch (Exception e){
                log.error("虚拟仓调整单主单反审核失败",e);
                VirtualAdjustEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "虚拟仓调整单主单不存在, 反审核失败");
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
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:delete",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "虚拟仓调整单主表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<VirtualAdjustEntity> list = virtualAdjustService.lambdaQuery().in(VirtualAdjustEntity::getId, ids).list();
		Map<String, VirtualAdjustEntity> idEntityMap = list.stream().collect(Collectors.toMap(VirtualAdjustEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = virtualAdjustService.delete(id);
            }catch (Exception e){
                log.error("虚拟仓调整单主单删除失败",e);
                VirtualAdjustEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "虚拟仓调整单主单不存在, 删除失败");
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
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:invalid",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "虚拟仓调整单主表作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<VirtualAdjustEntity> list = virtualAdjustService.lambdaQuery().in(VirtualAdjustEntity::getId, ids).list();
		Map<String, VirtualAdjustEntity> idEntityMap = list.stream().collect(Collectors.toMap(VirtualAdjustEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = virtualAdjustService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("虚拟仓调整单主单作废失败",e);
                VirtualAdjustEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "虚拟仓调整单主单不存在, 作废失败");
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
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:cancelProcess",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "虚拟仓调整单主表撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<VirtualAdjustEntity> list = virtualAdjustService.lambdaQuery().in(VirtualAdjustEntity::getId, ids).list();
        Map<String, VirtualAdjustEntity> idEntityMap = list.stream().collect(Collectors.toMap(VirtualAdjustEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = virtualAdjustService.cancelProcess(id);
            }catch (Exception e){
                log.error("虚拟仓调整单主单撤回流程失败",e);
                VirtualAdjustEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "虚拟仓调整单主单不存在, 撤回流程失败");
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
    * @author zdy
    * @date:  2025-06-09
    * @param id
    * @return ApiResult<VirtualAdjustDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:view",
            serviceClass = VirtualAdjustService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<VirtualAdjustDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(virtualAdjustService.view(id));
    }

    /**
    * 导出Excel数据
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualAdjust:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "虚拟仓调整单主表导出Excel数据")
    public ApiResult<Object> exportList(@RequestBody @Validated VirtualAdjustDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean result = virtualAdjustService.exportList(dto, response);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载虚拟仓调整导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        virtualAdjustService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入Excel
     * @author zdy
     * @date: 2024/8/14 9:39
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    @PostMapping("/importFile")
    public ApiResult<VirtualAdjustDTO.ImportDTO> importFile(@ModelAttribute @Validated VirtualAdjustDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        VirtualAdjustDTO.ImportDTO dto = virtualAdjustService.importFile(excelImportDTO.getExcelFile(),excelImportDTO.getDetailList(),response);
        return success(dto);
    }
}
