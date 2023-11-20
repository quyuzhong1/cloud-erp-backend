package com.erp.server.dmp.pull.schedule;

import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@EnableScheduling
public class PullGoodCangJob {

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 拉取goodCang任务
     */
    @XxlJob("goodCangExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(PlatformDictEnum.GOOD_CANG.getCode());
        });
    }

}
