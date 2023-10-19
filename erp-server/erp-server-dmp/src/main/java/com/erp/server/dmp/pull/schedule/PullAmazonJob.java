package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.TaskConstant;
import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
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
public class PullAmazonJob {

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 拉取亚马逊任务
     */
    @XxlJob("amazonExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(PlatformDictEnum.AMAZON.getCode());
        });
    }

    /**
     * 拉取亚马逊报表任务
     */
    @XxlJob("amazonReportExecute")
    public void reportExecute() {

    }


}
