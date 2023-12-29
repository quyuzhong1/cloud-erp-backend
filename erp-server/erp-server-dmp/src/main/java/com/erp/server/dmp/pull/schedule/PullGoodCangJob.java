package com.erp.server.dmp.pull.schedule;

import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    @XxlJob("pullGoodCangExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> platformDataThread.executeTask(PlatformDictEnum.GOOD_CANG.getCode()));
    }


    @XxlJob("goodCangCleanExecute")
    public void goodCangCleanExecute() {
        List<CleanDataTableEnum> platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.GOOD_CANG.getCode());
        if (CollectionUtils.isNotEmpty(platforms)){
            platforms.forEach(cleanDataTableEnum -> {
                JobTaskDTO jobTaskDTO = new JobTaskDTO();
                jobTaskDTO.setPlatformCategory(cleanDataTableEnum.getCategory());
                jobTaskDTO.setDictPlatform(cleanDataTableEnum.getPlatform());
                jobTaskDTO.setBillType(cleanDataTableEnum.getBusiness());
                try {
                    XxlJobHelper.log("开始清洗：{}类{}数据", cleanDataTableEnum.getPlatform(),cleanDataTableEnum.getBusiness());
                    platformDataThread.cleanOrder(jobTaskDTO);
                    XxlJobHelper.log("清洗完成：{}类{}数据", cleanDataTableEnum.getPlatform(),cleanDataTableEnum.getBusiness());
                }catch (Exception e){
                    XxlJobHelper.log("清洗异常：{}", e);
                }
            });
        }
    }
}
