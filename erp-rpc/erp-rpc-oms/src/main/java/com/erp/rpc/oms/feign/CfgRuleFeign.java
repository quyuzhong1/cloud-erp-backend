package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.tms.vo.request.LogisticsOrderRuleVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-oms", contextId = "cfgRuleFeign",configuration = {FeignErrorDecoder.class})
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


    @PostMapping("feign/cfgRule/handleRuleOrderLogistic")
    LogisticsOrderVO handleRuleOrderLogistic(@RequestBody LogisticsOrderRuleVO logisticsOrderRuleVO);
}
