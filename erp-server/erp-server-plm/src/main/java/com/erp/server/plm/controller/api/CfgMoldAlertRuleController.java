package com.erp.server.plm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.dto.CfgMoldAlertRuleDTO;
import com.erp.model.plm.entity.CfgMoldAlertRuleEntity;
import com.erp.server.plm.query.CfgMoldAlertRuleQueryHandler;
import com.erp.server.plm.service.CfgMoldAlertRuleService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模具预警策略
 *
 * @author jack
 * @since 2025-10-20
 */
@Slf4j
@RestController
@LogSystemModule("模具预警策略")
@RequestMapping("/cfgMoldAlertRule")
public class CfgMoldAlertRuleController extends BaseController {

    @Resource
    private CfgMoldAlertRuleService cfgMoldAlertRuleService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "模具预警策略新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgMoldAlertRuleDTO.AddDTO dto) {
        return success(cfgMoldAlertRuleService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-10-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具预警策略修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:cfgMoldAlertRule:update",
        serviceClass = CfgMoldAlertRuleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgMoldAlertRuleDTO.UpdateDTO dto) {
        cfgMoldAlertRuleService.update(dto);
        return success();
    }



    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldAlertRule:paging",
            tableAlias = "cmr"
    )
    public ApiResult<List<CfgMoldAlertRuleDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgMoldAlertRuleService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<PagingVO<CfgMoldAlertRuleDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldAlertRule:paging",
            tableAlias = "cmr"
    )
    @WebAdvanceQuery(handler = CfgMoldAlertRuleQueryHandler.class)
    public ApiResult<PagingVO<CfgMoldAlertRuleDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgMoldAlertRuleDTO.PagingParamDTO> dto) {
        return success(cfgMoldAlertRuleService.paging(dto));
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-10-10
     * @param id
     * @return ApiResult<CfgMoldAlertRuleDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldAlertRule:view",
            serviceClass = CfgMoldAlertRuleService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgMoldAlertRuleDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgMoldAlertRuleService.view(id));
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
            menuCode = "plm:cfgMoldAlertRule:delete",
            serviceClass = CfgMoldAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "模具返还策略删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldAlertRuleEntity> list = cfgMoldAlertRuleService.lambdaQuery().in(CfgMoldAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgMoldAlertRuleService.delete(id);
            }catch (Exception e){
                log.error("模具返还策略删除失败",e);
                CfgMoldAlertRuleEntity entity = idEntityMap.get(id);
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
            menuCode = "plm:cfgMoldAlertRule:invalid",
            serviceClass = CfgMoldAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "模具返还策略作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldAlertRuleEntity> list = cfgMoldAlertRuleService.lambdaQuery().in(CfgMoldAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = cfgMoldAlertRuleService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("模具返还策略作废失败",e);
                CfgMoldAlertRuleEntity entity = idEntityMap.get(id);
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
     * 禁用
     * @author jack
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldAlertRule:disabled",
            serviceClass = CfgMoldAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具返还策略禁用")
    public ApiResult<List<BatchResultDTO>> batchDisabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldAlertRuleEntity> list = cfgMoldAlertRuleService.lambdaQuery().in(CfgMoldAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = cfgMoldAlertRuleService.disabled(id);
            }catch (Exception e){
                log.error("模具返还策略禁用失败",e);
                CfgMoldAlertRuleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "模具返还策略不存在, 禁用失败");
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
     * 启用
     * @author jack
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldAlertRule:disabled",
            serviceClass = CfgMoldAlertRuleService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具返还策略启用")
    public ApiResult<List<BatchResultDTO>> batchEnable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgMoldAlertRuleEntity> list = cfgMoldAlertRuleService.lambdaQuery().in(CfgMoldAlertRuleEntity::getId, ids).list();
        Map<String, CfgMoldAlertRuleEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgMoldAlertRuleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = cfgMoldAlertRuleService.enable(id);
            }catch (Exception e){
                log.error("模具返还策略启用失败",e);
                CfgMoldAlertRuleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "模具返还策略不存在, 启用失败");
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
     * 启用/禁用
     * @author jack
     * @date:  2025-10-16
     * @param id
     * @return ApiResult<BatchResultDTO>
     */
    @GetMapping("/changeDisable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:cfgMoldAlertRule:disabled",
            serviceClass = CfgMoldAlertRuleService.class,
            keyIdName = "id")
    public ApiResult<BatchResultDTO> changeDisable(@RequestParam("id") String id) {
        CfgMoldAlertRuleEntity entity = cfgMoldAlertRuleService.lambdaQuery().eq(CfgMoldAlertRuleEntity::getId, id).one();
        BatchResultDTO result;
        try {
            result = cfgMoldAlertRuleService.changeDisable(entity);
        }catch (Exception e){
            log.error("模具返还策略启用失败",e);
            if (ObjectUtil.isEmpty(entity)) {
                result = BatchResultDTO.fail(id, id, "模具返还策略不存在, 启用失败");
            }else {
                result = BatchResultDTO.fail(entity.getId(), entity.getMoldCode(), e.getMessage());
            }
        }
        return result.getSuccess()? success(result) : failure(result);
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
    @WebAdvanceQuery(handler = CfgMoldAlertRuleQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated CfgMoldAlertRuleDTO.PagingParamDTO dto, HttpServletResponse response) {
        cfgMoldAlertRuleService.exportList(dto, response);
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
        Boolean result = cfgMoldAlertRuleService.importFile(dto);
        return result ? success() : failure();
    }

}
