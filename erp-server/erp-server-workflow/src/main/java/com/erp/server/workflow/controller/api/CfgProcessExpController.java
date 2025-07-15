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
import com.erp.server.workflow.service.CfgProcessExpService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgProcessExpDTO;

import java.util.List;

/**
 * 流程设置审核条件
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("流程设置审核条件")
@RequestMapping("/cfgProcessExp")
public class CfgProcessExpController extends BaseController {

    @Resource
    private CfgProcessExpService cfgProcessExpService;

    /**
     * 审核条件详情
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: BaseIdDTO
     * @return: List<CfgProcessExpDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<List<CfgProcessExpDTO.ViewDTO>> view(@RequestParam(value = "ruleId") String ruleId) {
        return success(cfgProcessExpService.view(ruleId));
    }
}
