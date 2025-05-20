package com.erp.server.workflow.controller.api;


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
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;

import java.util.List;

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
    * @author hcg
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "三方审批定义新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ThirdProcessDefinitionDTO.AddDTO dto) {
        return success(thirdProcessDefinitionService.add(dto));
    }

    /**
    * 修改
    * @author hcg
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
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
     * 飞书流程对应的流程名称
     * @return
     */
    @GetMapping("/drop/down")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方审批定义")
    public ApiResult<List<ThirdProcessDefinitionDTO.DropDownDTO>> dropDown() {
        return success(thirdProcessDefinitionService.dropDown());
    }
}
