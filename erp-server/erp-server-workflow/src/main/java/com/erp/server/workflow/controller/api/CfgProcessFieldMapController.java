package com.erp.server.workflow.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;

import java.util.List;

/**
 * 流程设置字段配置
 *
 * type = 执行条件的type值，现在只有erpProcess和fsProcess
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("流程设置字段配置")
@RequestMapping("/cfgProcessFieldMap")
public class CfgProcessFieldMapController extends BaseController {

    @Resource
    private CfgProcessFieldMapService cfgProcessFieldMapService;

    /**
     * 字段配置详情
     * type = 执行条件的type值，现在只有erpProcess和fsProcess
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: processDefinitionId
     * @return: List<CfgProcessFieldMapDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<List<CfgProcessFieldMapDTO.ViewDTO>> view(@RequestParam(value = "processDefinitionId") @Validated String processDefinitionId,
                                                               @RequestParam(value = "type") @Validated String type) {
        return success(cfgProcessFieldMapService.view(processDefinitionId, type));
    }
}
