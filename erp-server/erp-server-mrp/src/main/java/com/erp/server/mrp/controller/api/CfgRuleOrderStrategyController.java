package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleOrderStrategyDTO;
import com.erp.server.mrp.service.CfgRuleOrderStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 策略（规则设置）
 *
 * @author will
 * @since 2024-10-12
 */
@Slf4j
@RestController
@LogSystemModule("策略（规则设置）")
@RequestMapping("/cfgRuleOrderStrategy")
public class CfgRuleOrderStrategyController extends BaseController {

    @Resource
    private CfgRuleOrderStrategyService cfgRuleOrderStrategyService;

    /**
    * 修改
    * @author will
    * @date:  2024-10-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "策略（规则设置）修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:cfgRuleOrderStrategy:update",
        serviceClass = CfgRuleOrderStrategyService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleOrderStrategyDTO.UpdateDTO dto) {
        cfgRuleOrderStrategyService.update(dto);
        return success();
    }

    /**
     * 查询详情
     * @author will
     * @date 2024/10/12 11:23
     * @param platformType 
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleOrderStrategyDTO.ViewDTO> view(@RequestParam("platformType") String platformType) {
        return success(cfgRuleOrderStrategyService.view(platformType));
    }
}
