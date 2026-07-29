package com.erp.server.wms.schedule;

import com.erp.server.wms.service.SoReturnPrestockService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 预入库单强制关闭定时任务。
 * <p>业务目的：每月1号23:50，将当月剩余仍无人认领的预入库单强制关闭，之后不可再关联店铺与售后单。</p>
 * <p>调度：由 XXL-JOB 按 Cron 触发，建议 Cron 表达式 {@code 0 50 23 1 * ?}（每月1号23:50执行）。</p>
 *
 * @Author auto
 * @Date 2026-07-07
 **/
@Component
@Slf4j
@EnableScheduling
public class SoReturnPrestockForceCloseJob {

    @Resource
    private SoReturnPrestockService soReturnPrestockService;

    /**
     * 强制关闭剩余未认领的预入库单
     */
    @XxlJob("soReturnPrestockForceCloseJob")
    public ReturnT<String> soReturnPrestockForceCloseJob() {
        XxlJobHelper.log("=====预入库单强制关闭 开始任务=====");
        long start = System.currentTimeMillis();
        int count = soReturnPrestockService.forceCloseUnclaimedPrestock();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("本次强制关闭预入库单数量：{}，主线程花费时间：{}ms", count, (end - start));
        XxlJobHelper.log("=====预入库单强制关闭 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
