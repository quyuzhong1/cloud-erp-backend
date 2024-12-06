package com.erp.server.tms.schedule;

import com.erp.server.tms.service.SmallBagCostAllocationService;
import com.xxl.job.core.handler.annotation.XxlJob;

import javax.annotation.Resource;

public class LogisticsLargeJob {
    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;
    /**
     * 小包费用分摊自动生成物流大表
     */
    @XxlJob("smallBagAllocationToLogisticsLarge")
    public void smallBagAllocationToLogisticsLarge() {


    }
}
