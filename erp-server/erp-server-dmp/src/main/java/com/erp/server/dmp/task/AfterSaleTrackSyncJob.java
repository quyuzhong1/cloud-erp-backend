package com.erp.server.dmp.task;

import com.common.core.exception.ServiceException;
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
        try {
            afterSaleService.syncAfterSaleTrackStatus();
        } catch (Exception e) {
            String msg = (e instanceof ServiceException) ? e.getMessage() : "寄修单物流轨迹状态同步异常";
            XxlJobHelper.log("寄修单物流轨迹状态同步失败：{}", msg);
            log.warn("寄修单物流轨迹状态同步失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, msg);
        }
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====寄修单物流轨迹状态同步 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
