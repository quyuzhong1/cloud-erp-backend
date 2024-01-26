package com.erp.server.tms.schedule;

import com.common.business.enums.LogisticsPlatformEnum;
import com.erp.server.tms.service.LogisticsService;
import com.erp.server.tms.service.TransferDeclareService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 中转报关定时器
 */
@Component
@Slf4j
@EnableScheduling
public class TransferDeclareJob {

    @Resource
    private TransferDeclareService transferDeclareService;

    /**
     * 同步速卖通卖家地址
     *
     * @return
     */
    @XxlJob("TransferDeclareJob")
    public ReturnT declareAutoGenerationJob() {
        XxlJobHelper.log("====开始同步速卖通卖家地址====");
        transferDeclareService.declareAutoGenerationJob();
        XxlJobHelper.log("====结束同步速卖通卖家地址====");
        return ReturnT.SUCCESS;
    }
}
