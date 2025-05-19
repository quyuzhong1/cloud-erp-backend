package com.erp.server.workflow.controller.api;


import com.erp.model.sys.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
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
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import java.util.List;

/**
 * 查询option配置表(数大臣单据字段)
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@RestController
@LogSystemModule("查询option配置表(数大臣单据字段)")
@RequestMapping("/cfgQueryOption")
public class CfgQueryOptionController extends BaseController {

    @Resource
    private CfgQueryOptionService cfgQueryOptionService;

    /**
     *
     * @return
     */
    @GetMapping("/processDefinition/drop/down")
    public ApiResult<List<CfgQueryOptionDTO.ViewDTO>> proDropDown(@RequestParam(value = "bussinessKey") String bussinessKey) {
        return success(cfgQueryOptionService.proDropDown(bussinessKey));
    }

    /**
     *
     * @return
     */
    @GetMapping("/cfgApproveSync/drop/cfgApproveSyncDropDown")
    public ApiResult<List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO>> cfgApproveSyncDropDown(@RequestParam(value = "bussinessKey") String bussinessKey) {
        return success(cfgQueryOptionService.cfgApproveSyncDropDown(bussinessKey));
    }
}
