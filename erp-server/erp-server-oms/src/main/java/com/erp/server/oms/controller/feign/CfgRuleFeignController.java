package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.server.oms.service.CfgRuleOrderHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/feign/cfgRule")
public class CfgRuleFeignController extends BaseController {

    @Resource
    private CfgRuleOrderHandleService cfgRuleOrderHandleService;


    /**
     * 获取订单处理规则结果
     * @author Will
     * @date: 2024/5/9 16:36
     * @param map
     * @return RuleMatchDTO
     */
    @PostMapping("/getRuleOrderHandleMatchResult")
    public CfgRuleOrderHandleDTO.RuleMatchDTO getRuleOrderHandleMatchResult(@RequestBody Map<String, Object> map) {
        return cfgRuleOrderHandleService.getRuleOrderHandleMatchResult(map);
    }

}
