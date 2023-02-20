package com.erp.server.dmp.pull.schedule;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.constant.TaskConstant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.ThreadFactory;

@Component
@Slf4j
@EnableScheduling
public class PullKindeeJob {

    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // 拉取金蝶数据任务
    //@Scheduled(cron = "*/5 * * * * ?")
    @XxlJob("kindeeExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() ->{
            pullErpDateThread.executeTask(TaskConstant.KINGDEE_PULL_DATA_TASK);
        });
    }
}
