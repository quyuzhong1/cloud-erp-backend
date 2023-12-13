package com.erp.server.dmp.pull.schedule;

import com.common.business.constant.TaskConstant;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@EnableScheduling
public class PullMabang24HourJob {

    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // 拉取马帮最近24小时数据任务
    @XxlJob("mabang24HourExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> {
            pullErpDateThread.executeTask(TaskConstant.MABANG_PULL_DATA_TASK_24);
        });
    }
}
