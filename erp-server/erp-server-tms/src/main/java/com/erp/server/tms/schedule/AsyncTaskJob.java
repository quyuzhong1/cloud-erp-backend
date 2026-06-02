package com.erp.server.tms.schedule;

import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jack
 * @ClassName AsyncTaskJob
 * @description: 异步任务状态更新
 * @date 2026-01-30
 */
@Component
@Slf4j
public class AsyncTaskJob {

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    /**
     *
     *异步任务状态更新
     * @return
     */
    @XxlJob("TmsAsyncTaskJob")
    public ReturnT<String> TmsAsyncTaskJob() {
        asyncTaskRecordService.startTask();
        return ReturnT.SUCCESS;
    }
}
