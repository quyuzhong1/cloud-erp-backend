package com.erp.server.dmp.pull.schedule;

import com.erp.model.dmp.constant.TaskConstant;
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
public class PullImlJob {
    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 拉取艾姆勒数据任务
     */
    @XxlJob("omsImlExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(()->{
            pullErpDateThread.executeTask(TaskConstant.IML_PULL_DATA_TASK);
        });
    }
}
