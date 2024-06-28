package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.CfgRuleOutService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.CfgRuleOutDTO;

/**
 * 出库配置规则
 *
 * @author lrp
 * @since 2024-06-28
 */
@Slf4j
@RestController
@LogSystemModule("出库配置规则")
@RequestMapping("/cfgRuleOut")
public class CfgRuleOutController extends BaseController {

    @Resource
    private CfgRuleOutService cfgRuleOutService;

    /**
    * 新增
    * @author lrp
    * @date:  2024-06-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "出库配置规则新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleOutDTO.AddDTO dto) {
        return success(cfgRuleOutService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-06-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "出库配置规则修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:cfgRuleOut:update",
        serviceClass = CfgRuleOutService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleOutDTO.UpdateDTO dto) {
        cfgRuleOutService.update(dto);
        return success();
    }



}
