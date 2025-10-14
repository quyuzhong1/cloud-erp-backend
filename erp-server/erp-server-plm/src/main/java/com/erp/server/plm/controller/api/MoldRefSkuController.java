package com.erp.server.plm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.utils.ExcelUtil;
import com.erp.server.plm.query.MoldRefSkuQueryHandler;
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
import com.erp.server.plm.service.MoldRefSkuService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.MoldRefSkuEntity;

/**
 * 模具关联sku
 *
 * @author jack
 * @since 2025-10-14
 */
@Slf4j
@RestController
@LogSystemModule("模具关联sku")
@RequestMapping("/moldRefSku")
public class MoldRefSkuController extends BaseController {

    @Resource
    private MoldRefSkuService moldRefSkuService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:paging",
            tableAlias = "mrs"
    )
    public ApiResult<List<MoldRefSkuDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(moldRefSkuService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-10-14
    * @param dto
    * @return ApiResult<PagingVO<MoldRefSkuDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:paging",
            tableAlias = "mrs"
    )
    @WebAdvanceQuery(handler = MoldRefSkuQueryHandler.class)
    public ApiResult<PagingVO<MoldRefSkuDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<MoldRefSkuDTO.PagingParamDTO> dto) {
        return success(moldRefSkuService.paging(dto));
    }
    /**
    * 提交审核
    * @author jack
    * @date:  2025-10-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:submit",
            serviceClass = MoldRefSkuService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "模具关联sku提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<MoldRefSkuEntity> list = moldRefSkuService.lambdaQuery().in(MoldRefSkuEntity::getId, ids).list();
		Map<String, MoldRefSkuEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldRefSkuEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = moldRefSkuService.submit(id);
            }catch (Exception e){
                log.error("模具关联sku 提交审核失败",e);
                MoldRefSkuEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "模具关联sku不存在, 提交失败");
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
    * @date:  2025-10-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:approve",
            serviceClass = MoldRefSkuService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "模具关联sku审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<MoldRefSkuEntity> list = moldRefSkuService.lambdaQuery().in(MoldRefSkuEntity::getId, ids).list();
		Map<String, MoldRefSkuEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldRefSkuEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = moldRefSkuService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("模具关联sku审核失败",e);
                MoldRefSkuEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "模具关联sku不存在, 审核失败");
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
    * @date:  2025-10-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:disApprove",
            serviceClass = MoldRefSkuService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "模具关联sku反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<MoldRefSkuEntity> list = moldRefSkuService.lambdaQuery().in(MoldRefSkuEntity::getId, ids).list();
		Map<String, MoldRefSkuEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldRefSkuEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = moldRefSkuService.disApprove(id);
            }catch (Exception e){
                log.error("模具关联sku反审核失败",e);
                MoldRefSkuEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "模具关联sku不存在, 反审核失败");
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
    * @date:  2025-10-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:delete",
            serviceClass = MoldRefSkuService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "模具关联sku删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<MoldRefSkuEntity> list = moldRefSkuService.lambdaQuery().in(MoldRefSkuEntity::getId, ids).list();
		Map<String, MoldRefSkuEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldRefSkuEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = moldRefSkuService.delete(id);
            }catch (Exception e){
                log.error("模具关联sku删除失败",e);
                MoldRefSkuEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "模具关联sku不存在, 删除失败");
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
    * 撤销
    * @author jack
    * @date:  2025-10-14
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:cancelProcess",
            serviceClass = MoldRefSkuService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "模具关联sku撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<MoldRefSkuEntity> list = moldRefSkuService.lambdaQuery().in(MoldRefSkuEntity::getId, ids).list();
        Map<String, MoldRefSkuEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldRefSkuEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = moldRefSkuService.cancelProcess(id);
            }catch (Exception e){
                log.error("模具关联sku撤回流程失败",e);
                MoldRefSkuEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "模具关联sku不存在, 撤回流程失败");
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
    * @date:  2025-10-14
    * @param id
    * @return ApiResult<MoldRefSkuDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:view",
            serviceClass = MoldRefSkuService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<MoldRefSkuDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(moldRefSkuService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-10-14
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldRefSku:export",
            tableAlias = "mrs"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具关联sku导出Excel数据")
    @WebAdvanceQuery(handler = MoldRefSkuQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated MoldRefSkuDTO.PagingParamDTO dto, HttpServletResponse response) {
        moldRefSkuService.exportList(dto, response);
        return success();
    }


    /**
     * 下载模板
     * @author jack
     * @date:  2025-10-10
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具关联SKU下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/moldRefSkuTemplate.xlsx";
        String standardExcelName = "moldRefSkuTemplate.xlsx";
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
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入模具关联SKU")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = moldRefSkuService.importFile(dto);
        return result ? success() : failure();
    }


}
