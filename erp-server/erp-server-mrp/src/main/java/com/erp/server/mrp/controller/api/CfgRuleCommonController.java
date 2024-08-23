package com.erp.server.mrp.controller.api;


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
import com.erp.server.mrp.service.CfgRuleCommonService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;

/**
 * 公共配置（规则设置）
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("公共配置（规则设置）")
@RequestMapping("/cfgRuleCommon")
public class CfgRuleCommonController extends BaseController {

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "公共配置（规则设置）新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleCommonDTO.AddDTO dto) {
        return success(cfgRuleCommonService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "公共配置（规则设置）修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:cfgRuleCommon:update",
        serviceClass = CfgRuleCommonService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleCommonDTO.UpdateDTO dto) {
        cfgRuleCommonService.update(dto);
        return success();
    }



}
