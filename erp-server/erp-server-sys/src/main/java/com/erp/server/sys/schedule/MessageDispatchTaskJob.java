package com.erp.server.sys.schedule;

import com.erp.server.sys.service.MessageDispatchTaskService;
import com.erp.server.sys.service.support.NoticeStreamEmitterManager;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class MessageDispatchTaskJob {

    @Resource
    private MessageDispatchTaskService messageDispatchTaskService;

    @Resource
    private NoticeStreamEmitterManager noticeStreamEmitterManager;

    @XxlJob("messageDispatchTaskFallbackJob")
    public ReturnT<String> dispatchMessageTasksFallback() {
        XxlJobHelper.log("====messageDispatchTaskFallbackJob 开始任务=====");
        List<String> taskIds = messageDispatchTaskService.listDueTaskIds(20);
        for (String taskId : taskIds) {
            XxlJobHelper.log("补偿执行消息分发任务, taskId={}", taskId);
            messageDispatchTaskService.executeTaskAsync(taskId);
        }
        XxlJobHelper.log("====messageDispatchTaskFallbackJob 结束任务, taskCount={}=====", taskIds.size());
        return ReturnT.SUCCESS;
    }

    @Scheduled(initialDelay = 25000, fixedDelay = 25000)
    public void heartbeatNoticeStream() {
        noticeStreamEmitterManager.heartbeat();
    }
}
