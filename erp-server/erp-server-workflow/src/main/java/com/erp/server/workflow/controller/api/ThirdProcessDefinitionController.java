package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgThirdProcessDTO;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 三方审批定义
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("三方审批定义")
@RequestMapping("/thirdProcessDefinition")
public class ThirdProcessDefinitionController extends BaseController {

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hcg
     * @date: 2025-05-12
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "三方审批定义新增")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:thirdProcessDefinition:update",
            serviceClass = ThirdProcessDefinitionService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ThirdProcessDefinitionDTO.AddDTO dto) {
        return success(thirdProcessDefinitionService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author hcg
     * @date: 2025-05-12
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批定义修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:thirdProcessDefinition:update",
            serviceClass = ThirdProcessDefinitionService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ThirdProcessDefinitionDTO.UpdateDTO dto) {
        thirdProcessDefinitionService.update(dto);
        return success();
    }

    /**
     * 分页
     *
     * @param
     * @return ApiResult
     * @author hcg
     * @date: 2025-05-12
     */
    @PostMapping("/page")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ThirdProcessDefinitionDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ThirdProcessDefinitionDTO.PagingParamDTO> pagingParamDTO) {
        return success(thirdProcessDefinitionService.paging(pagingParamDTO));
    }

    /**
     * 详情
     *
     * @param id
     * @return ApiResult
     * @author hcg
     * @date: 2025-05-23
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:thirdProcessDefinition:view",
            serviceClass = ThirdProcessDefinitionService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ThirdProcessDefinitionDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(thirdProcessDefinitionService.view(id));
    }

    /**
     * 删除
     *
     * @return ApiResult<Boolean>
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除流程定义")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:thirdProcessDefinition:delete",
            serviceClass = ThirdProcessDefinitionService.class,
            keyIdName = "ids")
    @PostMapping("/delete")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ThirdProcessDefinitionEntity> list = thirdProcessDefinitionService.lambdaQuery().in(ThirdProcessDefinitionEntity::getId, ids).list();
        Map<String, ThirdProcessDefinitionEntity> idEntityMap = list.stream().collect(Collectors.toMap(ThirdProcessDefinitionEntity::getId, w -> w));
        for (String ruleId : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = thirdProcessDefinitionService.delete(ruleId);
            } catch (Exception e) {
                log.error("三方审批定义删除失败", e);
                ThirdProcessDefinitionEntity entity = idEntityMap.get(ruleId);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(ruleId, ruleId, "三方审批定义不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getApprovalCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量启用/禁用
     *
     * @param dto
     * @return ApiResult
     * @author hcg
     * @date: 2025-05-23
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:thirdProcessDefinition:enable",
            serviceClass = ThirdProcessDefinitionService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批定义启用/停用")
    public ApiResult<List<BatchResultDTO>> enable(@RequestBody @Validated CfgThirdProcessDTO.EnableStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ThirdProcessDefinitionEntity> list = thirdProcessDefinitionService.lambdaQuery().in(ThirdProcessDefinitionEntity::getId, ids).list();
        Map<String, ThirdProcessDefinitionEntity> idEntityMap = list.stream().collect(Collectors.toMap(ThirdProcessDefinitionEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = thirdProcessDefinitionService.enable(id, dto.getEnableStatus());
            } catch (Exception e) {
                log.error("三方审批生成更新失败", e);
                ThirdProcessDefinitionEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "三方审批生成不存在, 更新失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     *
     * @param dto
     * @return ApiResult
     * @author hcg
     * @date: 2025-05-23
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:thirdProcessDefinition:export",
            tableAlias = "ctp"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated ThirdProcessDefinitionDTO.PagingParamDTO dto, HttpServletResponse response) {
        thirdProcessDefinitionService.exportList(dto, response);
        return success();
    }

    /**
     * 飞书流程下拉（name：value）
     * type = push  or pull
     *
     * @return
     */
    @GetMapping("/drop/down")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批定义")
    public ApiResult<List<ThirdProcessDefinitionDTO.DropDownDTO>> dropDown(@RequestParam("type") @Validated String type) {
        return success(thirdProcessDefinitionService.dropDown(type));
    }
}
