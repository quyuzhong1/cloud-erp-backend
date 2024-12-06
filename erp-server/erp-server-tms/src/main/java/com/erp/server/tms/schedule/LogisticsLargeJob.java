package com.erp.server.tms.schedule;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.tms.entity.LogisticsLargeEntity;
import com.erp.model.tms.entity.SmallBagCostAllocationMainEntity;
import com.erp.server.tms.service.LogisticsLargeService;
import com.erp.server.tms.service.SmallBagCostAllocationMainService;
import com.erp.server.tms.service.SmallBagCostAllocationService;
import com.xxl.job.core.handler.annotation.XxlJob;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class LogisticsLargeJob {
    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Resource
    private LogisticsLargeService logisticsLargeService;

    /**
     * 小包费用分摊自动生成物流大表
     */
    @XxlJob("smallBagAllocationToLogisticsLarge")
    public void smallBagAllocationToLogisticsLarge() {
        List<LogisticsLargeEntity> largeEntityList = logisticsLargeService.lambdaQuery()
                .eq(LogisticsLargeEntity::getSourceType, SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())
                .ge(LogisticsLargeEntity::getReconciliationMonth, LocalDate.now().minusMonths(3))
                .list();

        List<String> ids = largeEntityList.stream().map(LogisticsLargeEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SmallBagCostAllocationMainEntity> list = smallBagCostAllocationMainService.list();


    }
}
