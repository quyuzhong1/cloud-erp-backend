package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 出库中转配置
 * @author will
 * @date 2024/7/18 21:16
 */
@FeignClient(name = "erp-wms", contextId = "cfgRuleOut" ,configuration = {FeignErrorDecoder.class})
public interface CfgRuleOutFeign {

    /**
     * 是否匹配中转规则
     * @author will
     * @date 2024/7/18 21:17
     * @param ruleDTO
     * @return Boolean
     */
    @PostMapping("feign/cfgRuleOut/matchTransferRule")
    CfgRuleOutDTO.MatchTransferResultDTO matchTransferRule(@RequestBody CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO);
}
