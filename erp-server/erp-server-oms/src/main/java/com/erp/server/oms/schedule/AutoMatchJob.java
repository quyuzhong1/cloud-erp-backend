package com.erp.server.oms.schedule;

import com.erp.server.oms.service.SkuMappingRuleService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * SKU自动匹配JOB
 */
@Component
@Slf4j
public class AutoMatchJob {

    @Resource
    private SkuMappingRuleService skuMappingRuleService;

    /**
     * 刷新速卖通的token
     */
    @XxlJob("skuAutoMatchJob")
    public void skuAutoMatchJob() {
        skuMappingRuleService.handleSkuMapping();
    }
}
