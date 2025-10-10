package com.erp.server.plm.controller.api;


import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.dto.MoldRefSkuDTO;
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
import com.erp.server.plm.service.MoldInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.MoldInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.MoldInfoEntity;

/**
 * 模具档案
 *
 * @author jack
 * @since 2025-10-10
 */
@Slf4j
@RestController
@LogSystemModule("模具档案")
@RequestMapping("/moldInfo")
public class MoldInfoController extends BaseController {

    @Resource
    private MoldInfoService moldInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "模具档案新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated MoldInfoDTO.AddDTO dto) {
        return success(moldInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具档案修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:moldInfo:update",
        serviceClass = MoldInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated MoldInfoDTO.UpdateDTO dto) {
        moldInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:paging",
            tableAlias = "mi"
    )
    public ApiResult<List<MoldInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(moldInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return ApiResult<PagingVO<MoldInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:paging",
            tableAlias = "mi"
    )
    public ApiResult<PagingVO<MoldInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<MoldInfoDTO.PagingParamDTO> dto) {
        return success(moldInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated MoldInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = moldInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:updateAndSubmit",
            serviceClass = MoldInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated MoldInfoDTO.UpdateDTO dto) {
        moldInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:submit",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "模具档案提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<MoldInfoEntity> list = moldInfoService.lambdaQuery().in(MoldInfoEntity::getId, ids).list();
		Map<String, MoldInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = moldInfoService.submit(id);
            }catch (Exception e){
                log.error("模具档案 提交审核失败",e);
                MoldInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "模具档案不存在, 提交失败");
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
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:approve",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "模具档案审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<MoldInfoEntity> list = moldInfoService.lambdaQuery().in(MoldInfoEntity::getId, ids).list();
		Map<String, MoldInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = moldInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("模具档案审核失败",e);
                MoldInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "模具档案不存在, 审核失败");
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
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:disApprove",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "模具档案反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<MoldInfoEntity> list = moldInfoService.lambdaQuery().in(MoldInfoEntity::getId, ids).list();
		Map<String, MoldInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = moldInfoService.disApprove(id);
            }catch (Exception e){
                log.error("模具档案反审核失败",e);
                MoldInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "模具档案不存在, 反审核失败");
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
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:delete",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "模具档案删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<MoldInfoEntity> list = moldInfoService.lambdaQuery().in(MoldInfoEntity::getId, ids).list();
		Map<String, MoldInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = moldInfoService.delete(id);
            }catch (Exception e){
                log.error("模具档案删除失败",e);
                MoldInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "模具档案不存在, 删除失败");
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
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:invalid",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "模具档案作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<MoldInfoEntity> list = moldInfoService.lambdaQuery().in(MoldInfoEntity::getId, ids).list();
		Map<String, MoldInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = moldInfoService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("模具档案作废失败",e);
                MoldInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "模具档案不存在, 作废失败");
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
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:cancelProcess",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "模具档案撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MoldInfoEntity> list = moldInfoService.lambdaQuery().in(MoldInfoEntity::getId, ids).list();
        Map<String, MoldInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = moldInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("模具档案撤回流程失败",e);
                MoldInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "模具档案不存在, 撤回流程失败");
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
    * @author jack
    * @date:  2025-10-10
    * @param id
    * @return ApiResult<MoldInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:view",
            serviceClass = MoldInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<MoldInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(moldInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:export",
            tableAlias = "mi"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具档案导出Excel数据")
    public ApiResult<Object> exportList(@RequestBody @Validated MoldInfoDTO.ExportDTO dto, HttpServletResponse response) {
        moldInfoService.exportList(dto, response);
        return success();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-10-10
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具档案下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/moldInfoTemplate.xlsx";
        String standardExcelName = "moldInfoTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     *  导入
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入模具档案")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = moldInfoService.importFile(dto);
        return result ? success() : failure();
    }
    /**
     * 批量关联sku
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchRefSku")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具关联sku")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldInfo:batchRefSku",
            serviceClass = MoldInfoService.class,
            keyIdName = "ids")
    public ApiResult<Object> batchRefSku(@RequestBody @Validated MoldInfoDTO.RefSkuDTO dto) {
        Boolean result =moldInfoService.batchRefSku(dto);
        return result ? success() : failure();
    }


}
