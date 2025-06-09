package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.CfgVirtualTransRulesDTO;
import com.erp.server.wms.service.CfgVirtualTransRulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 虚拟库存交易规则表
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟库存交易规则表")
@RequestMapping("/cfgVirtualTransRules")
public class CfgVirtualTransRulesController extends BaseController {

    @Resource
    private CfgVirtualTransRulesService cfgVirtualTransRulesService;

    /**
    * 新增
    * @author will
    * @date:  2024-06-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟库存交易规则表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgVirtualTransRulesDTO.AddDTO dto) {
        return success(cfgVirtualTransRulesService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-06-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟库存交易规则表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:cfgVirtualTransRules:update",
        serviceClass = CfgVirtualTransRulesService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated CfgVirtualTransRulesDTO.UpdateDTO dto) {
        cfgVirtualTransRulesService.update(dto);
        return success();
    }



}
