package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.CfgRuleOutService;
import com.common.core.controller.vo.ApiResult;
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
    * 新增或更新
    * @author lrp
    * @date:  2024-06-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/addOrUpdate")
    @LogAction(value = LogActionEnum.INSERT, desc = "出库配置规则新增或更新")
    public ApiResult<BaseResultDTO.AddDTO> addOrUpdate(@RequestBody @Validated CfgRuleOutDTO.CommonDTO dto) {
        return success(cfgRuleOutService.addOrUpdate(dto));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2024-06-28
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<CfgRuleOutDTO.CommonDTO> view() {
        return success(cfgRuleOutService.view());
    }

    /**
     * 匹配中转仓配置
     */
    @PostMapping("/matchTransferRule")
    public ApiResult<CfgRuleOutDTO.MatchTransferResultDTO> matchTransferRule(@RequestBody CfgRuleOutDTO.MatchTransferRuleDTO dto){
        CfgRuleOutDTO.MatchTransferResultDTO matchTransferResultDTO = cfgRuleOutService.matchTransferRule(dto);
        return success(matchTransferResultDTO);
    }
}
