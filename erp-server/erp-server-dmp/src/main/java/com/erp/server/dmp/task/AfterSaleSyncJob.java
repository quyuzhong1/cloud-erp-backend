package com.erp.server.dmp.task;

import com.erp.server.dmp.service.AfterSaleService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 售后申请单同步旺店通
 * @Author jack
 * @Date 2025-04-10
 **/
@Component
@Slf4j
@EnableScheduling
public class AfterSaleSyncJob {
    @Resource
    private AfterSaleService afterSaleService;

    /**
     * 售后申请单同步旺店通
     * @Author jack
     * @Date 2025-04-10
     **/
    @XxlJob("AfterSaleSyncJob")
    public ReturnT<String> AfterSaleSyncJob() {
        XxlJobHelper.log("====售后申请单同步旺店通 开始任务=====");
        long start = System.currentTimeMillis();
        afterSaleService.syncWdtToAfterSale();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====售后申请单同步旺店通 结束任务=====");
        return ReturnT.SUCCESS;
    }

}
