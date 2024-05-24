package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.TaskConstant;
import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private PlatformDataThread platformDataThread;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @XxlJob("wdtExecute")
    public void execute() {
//        threadPoolTaskExecutor.execute(() -> pullErpDateThread.executeTask(TaskConstant.WANGDIAN_PULL_DATA_TASK));

        // 分组查询
        List<String> groupIds = platformApiTaskService.findGroupIdByPlatform(TaskConstant.WANGDIAN_PULL_DATA_TASK);
        if (CollectionUtils.isEmpty(groupIds)) {
            XxlJobHelper.log("[拉取wangdian任务] 任务结束:无任务 =====");
            return;
        }
        groupIds.forEach(x -> threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(x, true);
        }));
    }

    @XxlJob("wdtCleanExecute")
    public void mabangCleanExecute() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("旺店通清洗任务参数：{}", jobParam);
        List<String> taskList = new ArrayList<>();
        if (StrUtil.isNotBlank(jobParam)) {
            taskList = Arrays.asList(jobParam.split(","));
        }
//        pullErpDateThread.executeCleanTask(TaskConstant.WANGDIAN_PULL_DATA_TASK, taskList);
    }
}
