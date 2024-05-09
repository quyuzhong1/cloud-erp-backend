package com.erp.rpc.oms.feign;

import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-oms", contextId = "cfgRule")
public interface CfgRuleFeign {

    /**
     * 获取订单处理规则结果
     * @author Will
     * @date: 2024/5/9 16:39
     * @param map
     * @return RuleMatchDTO
     */
    @PostMapping("feign/cfgRule/getRuleOrderHandleMatchResult")
    CfgRuleOrderHandleDTO.RuleMatchDTO getRuleOrderHandleMatchResult(@RequestBody Map<String, Object> map);


}
