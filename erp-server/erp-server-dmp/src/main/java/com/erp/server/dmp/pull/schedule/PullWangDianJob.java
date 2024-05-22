package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.TaskConstant;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class PullWangDianJob {


    @Resource
    private PullErpDateThread pullErpDateThread;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @XxlJob("wdtExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> pullErpDateThread.executeTask(TaskConstant.WANGDIAN_PULL_DATA_TASK));
    }

    @XxlJob("wdtCleanExecute")
    public void mabangCleanExecute() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("旺店通清洗任务参数：{}", jobParam);
        List<String> taskList = new ArrayList<>();
        if (StrUtil.isNotBlank(jobParam)) {
            taskList = Arrays.asList(jobParam.split(","));
        }
        pullErpDateThread.executeCleanTask(TaskConstant.WANGDIAN_PULL_DATA_TASK, taskList);
    }
}
