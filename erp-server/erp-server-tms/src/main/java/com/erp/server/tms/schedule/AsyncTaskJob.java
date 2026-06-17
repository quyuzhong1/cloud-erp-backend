package com.erp.server.tms.schedule;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
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
     * 每日扫描对账周期配置，预创建当月到期的自动异步任务（不派发 MQ）。
     * <p>
     * 创建与派发分离：本 Job 只负责 {@link TmsAsyncTaskRecordService#genAutoTask()}，
     * 到期执行由 {@link #TmsAsyncTaskJob()} 触发。
     *
     * @return 调度成功
     */
    @XxlJob("GenAutoTaskJob")
    public ReturnT<String> AutoGenAsyncTaskJob() {
        XxlJobHelper.log("====开始自动生成tms异步任务====");
        TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO summary = asyncTaskRecordService.genAutoTask();
        logGenAutoTaskSummaryToXxl(summary);
        return ReturnT.SUCCESS;
    }

    /**
     * 将 genAutoTask 汇总写入 XXL-JOB 执行日志，便于控制台排查。
     *
     * @param summary 服务层返回的生成汇总
     */
    private void logGenAutoTaskSummaryToXxl(TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO summary) {
        if (summary == null) {
            XxlJobHelper.log("====自动生成tms异步任务结束：无汇总结果====");
            return;
        }
        XxlJobHelper.log("====结束自动生成tms异步任务 耗时={}ms created={} skipped={} failed={}====",
            summary.getDurationMs(), summary.getCreatedCount(), summary.getSkippedCount(), summary.getFailedCount());
        if (CollUtil.isEmpty(summary.getItems())) {
            return;
        }
        for (TmsAsyncTaskRecordDTO.GenAutoTaskItemResult item : summary.getItems()) {
            XxlJobHelper.log("[自动生成任务] {} | {} | businessType={} methodType={} startTime={} taskId={} message={}",
                item.getTaskName(), item.getStatus(), item.getBusinessType(), item.getMethodType(),
                item.getStartTimeStr(), item.getTaskId(), item.getMessage());
        }
    }

    /**
     * 派发已到期的 {@code AUTO + PENDING} 自动任务（不处理超时清理与手动任务）。
     *
     * @return 调度成功
     */
    @XxlJob("TmsAsyncTaskJob")
    public ReturnT<String> TmsAsyncTaskJob() {
        asyncTaskRecordService.startTask();
        return ReturnT.SUCCESS;
    }

    /**
     * 异步任务 watchdog，负责超时和孤儿明细清理。
     */
    @XxlJob("TmsAsyncTaskWatchdogJob")
    public ReturnT<String> TmsAsyncTaskWatchdogJob() {
        asyncTaskRecordService.watchdogTask();
        return ReturnT.SUCCESS;
    }

}
