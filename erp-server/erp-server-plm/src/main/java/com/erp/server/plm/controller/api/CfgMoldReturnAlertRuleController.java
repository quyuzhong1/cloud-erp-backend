package com.erp.server.plm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import com.erp.server.plm.query.CfgMoldReturnAlertRuleQueryHandler;
import com.erp.server.plm.query.MoldInfoQueryHandler;
import com.erp.server.plm.service.MoldInfoService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.plm.service.CfgMoldReturnAlertRuleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模具返还策略
 *
 * @author jack
 * @since 2025-10-15
 */
@Slf4j
@RestController
@LogSystemModule("模具返还策略")
@RequestMapping("/cfgMoldReturnAlertRule")
public class CfgMoldReturnAlertRuleController extends BaseController {

    @Resource
    private CfgMoldReturnAlertRuleService cfgMoldReturnAlertRuleService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "模具返还策略新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgMoldReturnAlertRuleDTO.AddDTO dto) {
        return success(cfgMoldReturnAlertRuleService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-10-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具返还策略修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:cfgMoldReturnAlertRule:update",
        serviceClass = CfgMoldReturnAlertRuleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgMoldReturnAlertRuleDTO.UpdateDTO dto) {
        cfgMoldReturnAlertRuleService.update(dto);
        return success();
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldReturnAlertRule:paging",
            tableAlias = "cmr"
    )
    public ApiResult<List<CfgMoldReturnAlertRuleDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgMoldReturnAlertRuleService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldReturnAlertRule:paging",
            tableAlias = "cmr"
    )
    @WebAdvanceQuery(handler = CfgMoldReturnAlertRuleQueryHandler.class)
    public ApiResult<PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgMoldReturnAlertRuleDTO.PagingParamDTO> dto) {
        return success(cfgMoldReturnAlertRuleService.paging(dto));
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-10-10
     * @param id
     * @return ApiResult<CfgMoldReturnAlertRuleDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldReturnAlertRule:view",
            serviceClass = CfgMoldReturnAlertRuleService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgMoldReturnAlertRuleDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgMoldReturnAlertRuleService.view(id));
    }


    /**
     * 删除
     * @author jack
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldReturnAlertRule:delete",
            serviceClass = CfgMoldReturnAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "模具返还策略删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldReturnAlertRuleEntity> list = cfgMoldReturnAlertRuleService.lambdaQuery().in(CfgMoldReturnAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldReturnAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldReturnAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgMoldReturnAlertRuleService.delete(id);
            }catch (Exception e){
                log.error("模具返还策略删除失败",e);
                CfgMoldReturnAlertRuleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "模具返还策略不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getMoldCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 作废
     * @author jack
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldReturnAlertRule:invalid",
            serviceClass = CfgMoldReturnAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "模具返还策略作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldReturnAlertRuleEntity> list = cfgMoldReturnAlertRuleService.lambdaQuery().in(CfgMoldReturnAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldReturnAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldReturnAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = cfgMoldReturnAlertRuleService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("模具返还策略作废失败",e);
                CfgMoldReturnAlertRuleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "模具返还策略不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getMoldCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 启用禁用
     * @author jack
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldReturnAlertRule:updateStatus",
            serviceClass = CfgMoldReturnAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldReturnAlertRuleEntity> list = cfgMoldReturnAlertRuleService.lambdaQuery().in(CfgMoldReturnAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldReturnAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldReturnAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = cfgMoldReturnAlertRuleService.updateStatus(id,dto.getDisabled());
            }catch (Exception e){
                log.error("模具返还策略启用/禁用失败",e);
                CfgMoldReturnAlertRuleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "模具返还策略不存在, 启用/禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getMoldCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-10-16
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具返还策略导出Excel数据")
    @WebAdvanceQuery(handler = CfgMoldReturnAlertRuleQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated CfgMoldReturnAlertRuleDTO.PagingParamDTO dto, HttpServletResponse response) {
        cfgMoldReturnAlertRuleService.exportList(dto, response);
        return success();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-10-16
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具返还策略下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/cfgMoldReturnTemplate.xlsx";
        String standardExcelName = "cfgMoldReturnTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     *  导入
     * @author jack
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入模具返还策略")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = cfgMoldReturnAlertRuleService.importFile(dto);
        return result ? success() : failure();
    }
}
