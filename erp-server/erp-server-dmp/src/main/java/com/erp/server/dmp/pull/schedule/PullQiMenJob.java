package com.erp.server.dmp.pull.schedule;

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
import java.util.List;

/**
 * 奇门定时任务
 * @date 2024-06-11
 * @author tanmujin
 */
@Component
@Slf4j
@EnableScheduling
public class PullQiMenJob {
    @Resource
    private PlatformDataThread platformDataThread;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @XxlJob("qiMenExecute")
    public void execute(){
        List<String> groupIds = platformApiTaskService.findGroupIdByPlatform(PlatformDictEnum.QI_MEN.getCode());
        if (CollectionUtils.isEmpty(groupIds)) {
            XxlJobHelper.log("[拉取QiMen任务] 任务结束:无任务 =====");
            return;
        }
        groupIds.forEach(x -> threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(x, true);
        }));
    }
}
