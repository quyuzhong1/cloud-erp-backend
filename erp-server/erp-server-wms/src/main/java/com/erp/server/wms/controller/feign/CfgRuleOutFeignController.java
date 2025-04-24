package com.erp.server.wms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import com.erp.server.wms.service.CfgRuleOutService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/feign/cfgRuleOut")
public class CfgRuleOutFeignController extends BaseController {

    @Resource
    private CfgRuleOutService cfgRuleOutService;

    /**
     * 是否匹配中转规则
     * @author will
     * @date 2024/7/18 21:19
     * @param ruleDTO
     * @return Boolean
     */
    @PostMapping("/matchTransferRule")
    public CfgRuleOutDTO.MatchTransferResultDTO matchTransferRule(@RequestBody @Validated CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO) {
        return cfgRuleOutService.matchTransferRule(ruleDTO);
    }
}
