package com.erp.server.dmp.pull.schedule;

import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.enums.CleanDataTableEnum;
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

@Component
@Slf4j
@EnableScheduling
public class PullTikTokJob {
    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    /**
     * 拉取TikTok任务
     */
    @XxlJob("tikTokExecute")
    public void execute() {
        // 分组查询
        List<String> groupIds = platformApiTaskService.findGroupIdByPlatform(PlatformDictEnum.TIK_TOK.getCode());
        if (CollectionUtils.isEmpty(groupIds)) {
            XxlJobHelper.log("[拉取沃尔玛任务] 任务结束:无任务 =====");
            return;
        }
        groupIds.forEach(x -> threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(x, true);
        }));
    }

    @XxlJob("tikTokCleanExecute")
    public void tikTokCleanExecute() {
        List<CleanDataTableEnum> platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.TIK_TOK.getCode());
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
