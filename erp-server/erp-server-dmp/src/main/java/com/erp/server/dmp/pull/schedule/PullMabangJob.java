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
public class PullMabangJob {

    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // 拉取马帮数据任务
    //上午7:00~19:00更新时间
    //@Scheduled(cron = "*/10 * 0,1,2,3,4,5,6,20,21,22,23 * * ?")
    @XxlJob("mabangExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> {
            pullErpDateThread.executeTask(TaskConstant.MABANG_PULL_DATA_TASK);
        });
    }
}
