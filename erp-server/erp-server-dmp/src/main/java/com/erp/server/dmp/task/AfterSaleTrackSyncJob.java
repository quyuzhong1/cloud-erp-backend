package com.erp.server.dmp.task;

import com.erp.server.dmp.service.AfterSaleService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 寄修单商家寄出物流轨迹状态同步（Track123）
 */
@Component
@Slf4j
public class AfterSaleTrackSyncJob {

    @Resource
    private AfterSaleService afterSaleService;

    @XxlJob("AfterSaleTrackSyncJob")
    public ReturnT<String> afterSaleTrackSyncJob() {
        XxlJobHelper.log("====寄修单物流轨迹状态同步 开始任务=====");
        long start = System.currentTimeMillis();
        afterSaleService.syncAfterSaleTrackStatus();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====寄修单物流轨迹状态同步 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
