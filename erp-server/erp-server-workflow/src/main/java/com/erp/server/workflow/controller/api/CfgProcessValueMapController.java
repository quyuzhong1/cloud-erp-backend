package com.erp.server.workflow.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;

import java.util.List;

/**
 * 流程设置值映射
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("流程设置值映射")
@RequestMapping("/cfgProcessValueMap")
public class CfgProcessValueMapController extends BaseController {

    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    /**
     * 值映射详情
     * type = 执行条件的type值，现在只有erpProcess和fsProcess
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: thirdFieldId:thirdFieldId、approvalCode:processDefinitionId
     * @return: CfgInvoiceSettingDTO.ViewDTO
     **/
    @GetMapping("/view")
    public ApiResult<List<CfgProcessValueMapDTO.DropDownDTO>> view(@RequestParam(value = "thirdFieldId") @Validated String thirdFieldId, @RequestParam(value = "approvalCode") @Validated String approvalCode, @RequestParam(value = "type") @Validated String type){
        return success(cfgProcessValueMapService.view(thirdFieldId, approvalCode,type));
    }
}
