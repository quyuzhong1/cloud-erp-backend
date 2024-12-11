package com.erp.server.tms.schedule;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.SmallBagCostAllocationMainBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationMainReportStatusEnum;
import com.erp.server.tms.service.*;
import com.xxl.job.core.handler.annotation.XxlJob;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class LogisticsLargeJob {

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Resource
    private LogisticsLargeService logisticsLargeService;

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    /**
     * 小包费用分摊自动生成物流大表
     */
    @XxlJob("smallBagAllocationToLogisticsLarge")
    public void smallBagAllocationToLogisticsLarge() {
        List<SmallBagCostAllocationMainEntity> allocationMainEntityList = smallBagCostAllocationMainService.lambdaQuery()
                .eq(SmallBagCostAllocationMainEntity::getBigTableStatus, SmallBagCostAllocationMainBigTableStatusEnum.TODO.getCode())
                .eq(SmallBagCostAllocationMainEntity::getReportStatus, SmallBagCostAllocationMainReportStatusEnum.CONFIRMED.getCode())
                .orderByDesc(SmallBagCostAllocationMainEntity::getReportDate)
                .list();

        for (SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity : allocationMainEntityList) {
            logisticsLargeService.generateSmallBagCostAllocationTable(smallBagCostAllocationMainEntity);
        }
    }



    /**
     * 中转费用分摊自动生成物流大表
     */
    @XxlJob("transferDeclareCostAllocationToLogisticsLarge")
    public void transferDeclareCostAllocationToLogisticsLarge() {
        List<TransferDeclareCostAllocationMainEntity> allocationMainEntityList = transferDeclareCostAllocationMainService.lambdaQuery()
                .eq(TransferDeclareCostAllocationMainEntity::getBigTableStatus, SmallBagCostAllocationMainBigTableStatusEnum.TODO.getCode())
                .eq(TransferDeclareCostAllocationMainEntity::getReportStatus, SmallBagCostAllocationMainReportStatusEnum.CONFIRMED.getCode())
                .orderByDesc(TransferDeclareCostAllocationMainEntity::getReportDate)
                .list();

        for (TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity : allocationMainEntityList) {
//            logisticsLargeService.generateTransferCostAllocationTable(transferDeclareCostAllocationMainEntity);
        }
    }


    /**
     * 头程费用分摊自动生成物流大表
     */
    @XxlJob("firstMileCostAllocationToLogisticsLarge")
    public void firstMileCostAllocationToLogisticsLarge() {
/*        logisticsLargeService.lambdaQuery()
                .eq(LogisticsLargeEntity::getSourceType, SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())
                .
                .list();*/

        List<TransferDeclareCostAllocationMainEntity> allocationMainEntityList = transferDeclareCostAllocationMainService.lambdaQuery()
                .eq(TransferDeclareCostAllocationMainEntity::getBigTableStatus, SmallBagCostAllocationMainBigTableStatusEnum.TODO.getCode())
                .eq(TransferDeclareCostAllocationMainEntity::getReportStatus, SmallBagCostAllocationMainReportStatusEnum.CONFIRMED.getCode())
                .orderByDesc(TransferDeclareCostAllocationMainEntity::getReportDate)
                .list();

        for (TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity : allocationMainEntityList) {
//            logisticsLargeService.generateTransferCostAllocationTable(transferDeclareCostAllocationMainEntity);
        }
    }
}
