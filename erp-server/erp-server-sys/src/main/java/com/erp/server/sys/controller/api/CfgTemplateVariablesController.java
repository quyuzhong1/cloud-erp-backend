package com.erp.server.sys.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
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
import com.erp.server.sys.service.CfgTemplateVariablesService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.CfgTemplateVariablesDTO;

import java.util.List;

/**
 * 模板字段表
 *
 * @author jack
 * @since 2025-07-24
 */
@Slf4j
@RestController
@LogSystemModule("模板字段表")
@RequestMapping("/cfgTemplateVariables")
public class CfgTemplateVariablesController extends BaseController {

    @Resource
    private CfgTemplateVariablesService cfgTemplateVariablesService;


    /**
     * 根据模板类型查询模板字段
     * @author jack
     * @date:  2025-07-24
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/listByTemplateType")
    public ApiResult<List<CfgTemplateVariablesDTO.VariableGroupDTO>> listByTemplateType(@RequestBody @Validated CfgTemplateVariablesDTO.TemplateParamDTO dto) {
        return success(cfgTemplateVariablesService.listByTemplateType(dto));
    }



}
