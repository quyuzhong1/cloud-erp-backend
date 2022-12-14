package com.erp.server.dmp.pull.schedule;

import com.alibaba.fastjson2.JSONObject;
import com.erp.model.dmp.constant.TaskConstant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.thread.PullMabangDateThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

@Component
@Slf4j
@EnableScheduling
public class PullMabangJob {
    @Autowired
    private RedisTemplate<String, String> template;

    @Resource
    private PullMabangDateThread pullMabangDateThread;

    @Resource(name = "mabang")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // 拉取马帮数据任务
    //@Scheduled(cron = "*/5 * * * * ?")
    //上午7:00~19:00更新时间
    @Scheduled(cron = "* * 0,1,2,3,4,5,6,20,21,22,23 * * ? *")
    public void execute() {
        while (threadPoolTaskExecutor.getActiveCount() + 1 < threadPoolTaskExecutor.getMaxPoolSize()) {
            // 获取请求任务
            String o = template.opsForList().leftPop(TaskConstant.MABANG_PULL_DATA_TASK);
            if(ObjectUtils.isEmpty(o) || "null".equals(o)) {
                break;
            }
            JobTaskDTO orderJobTask = JSONObject.parseObject(o, JobTaskDTO.class);
            if (orderJobTask == null) {
                break;
            }
            pullMabangDateThread.pullOrder(orderJobTask);
        }
    }
}
