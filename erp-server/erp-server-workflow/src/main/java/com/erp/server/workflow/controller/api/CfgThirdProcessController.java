package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.server.workflow.handler.CfgProcessQueryHandler;
import com.erp.server.workflow.handler.CfgThirdProcessQueryHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.workflow.service.CfgThirdProcessService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgThirdProcessDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 三方审批生成
 *
 * @author hcg
 * @since 2025-05-23
 */
@Slf4j
@RestController
@LogSystemModule("三方审批生成")
@RequestMapping("/cfgThirdProcess")
public class CfgThirdProcessController extends BaseController {

    @Resource
    private CfgThirdProcessService cfgThirdProcessService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hcg
     * @date: 2025-05-23
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "三方审批生成新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Valid CfgThirdProcessDTO.AddDTO dto) {
        return success(cfgThirdProcessService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author hcg
     * @date: 2025-05-23
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批生成修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:update",
            serviceClass = CfgThirdProcessService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgThirdProcessDTO.UpdateDTO dto) {
        return success(cfgThirdProcessService.update(dto));
    }

    /**
     * 分页查询
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:paging",
            tableAlias = "ctp"
    )
    @WebAdvanceQuery(handler = CfgThirdProcessQueryHandler.class)
    public ApiResult<PagingVO<CfgThirdProcessDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgThirdProcessDTO.PagingParamDTO> dto) {
        return success(cfgThirdProcessService.paging(dto));
    }

    /**
     * tab查询
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/tabList")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批生成修改")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:paging",
            tableAlias = "ctp"
    )
    public ApiResult<List<CfgThirdProcessDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgThirdProcessService.tabList(dto));
    }

    /**
     * 详情
     * @author hcg
     * @date: 2025-05-23
     * @param id
     * @return ApiResult
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:view",
            serviceClass = CfgThirdProcessService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgThirdProcessDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgThirdProcessService.view(id));
    }

    /**
     * 批量删除
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:delete",
            serviceClass = CfgThirdProcessService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "三方审批生成删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgThirdProcessEntity> list = cfgThirdProcessService.lambdaQuery().in(CfgThirdProcessEntity::getId, ids).list();
        Map<String, CfgThirdProcessEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgThirdProcessEntity::getId, w -> w));
        for (String ruleId : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgThirdProcessService.delete(ruleId);
            }catch (Exception e){
                log.error("三方审批生成删除失败",e);
                CfgThirdProcessEntity entity = idEntityMap.get(ruleId);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(ruleId, ruleId, "三方审批生成不存在, 删除失败");
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
     * 批量启用/禁用
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:enable",
            serviceClass = CfgThirdProcessService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批生成启用/停用")
    public ApiResult<List<BatchResultDTO>> enable(@RequestBody @Validated  CfgThirdProcessDTO.EnableStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgThirdProcessEntity> list = cfgThirdProcessService.lambdaQuery().in(CfgThirdProcessEntity::getId, ids).list();
        Map<String, CfgThirdProcessEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgThirdProcessEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgThirdProcessService.enable(id,dto.getEnableStatus());
            }catch (Exception e){
                log.error("三方审批生成更新失败",e);
                CfgThirdProcessEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "三方审批生成不存在, 更新失败");
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
     * 导出
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgThirdProcess:export",
            tableAlias = "ctp"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = CfgThirdProcessQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated CfgThirdProcessDTO.PagingParamDTO dto, HttpServletResponse response) {
        cfgThirdProcessService.exportList(dto, response);
        return success();
    }
}
