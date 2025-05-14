package com.erp.server.workflow.controller.api;


import com.erp.model.workflow.dto.CfgProcessDTO;
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
import com.erp.server.workflow.service.CfgProcessRuleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;

import java.util.List;

/**
 * 流程设置执行条件
 *
 * @author hcg
 * @since 2025-05-13
 */
@Slf4j
@RestController
@LogSystemModule("流程设置执行条件")
@RequestMapping("/cfgProcessRule")
public class CfgProcessRuleController extends BaseController {

    @Resource
    private CfgProcessRuleService cfgProcessRuleService;

    /**
     * 新增
     * @author hcg
     * @date: 2025-05-13
     * @param dto
     * @return ApiResult<String>
     */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "流程设置执行条件新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated List<CfgProcessRuleDTO.AddOrUpdateDTO> dto) {
//        return success(cfgProcessRuleService.addOrUpdate(dto));
//    }
}
