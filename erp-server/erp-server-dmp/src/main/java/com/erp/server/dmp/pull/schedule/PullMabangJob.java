package com.erp.server.dmp.pull.schedule;

import com.alibaba.fastjson2.JSONObject;
import com.erp.model.dmp.constant.TaskConstant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@EnableScheduling
public class PullMabangJob {
    @Autowired
    private RedisTemplate<String, String> template;

    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // 拉取马帮数据任务
    //上午7:00~19:00更新时间
    //@Scheduled(cron = "*/10 * 0,1,2,3,4,5,6,20,21,22,23 * * ?")
    @XxlJob("mabangExecute")
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
            if(ObjectUtils.isEmpty(template.opsForValue().get(orderJobTask.getApiCode()+""))) {
                pullErpDateThread.pullOrder(orderJobTask);

                //redis调用时间限制
                template.opsForValue().set(orderJobTask.getApiCode()+"","text",10, TimeUnit.SECONDS);
            }else {
                template.opsForList().leftPush(TaskConstant.MABANG_PULL_DATA_TASK, JSONObject.toJSONString(orderJobTask));
            }


        }
    }
}
