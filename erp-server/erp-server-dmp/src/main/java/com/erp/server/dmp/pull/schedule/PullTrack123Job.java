package com.erp.server.dmp.pull.schedule;

import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zdy
 * @ClassName PullTrack123Job
 * @description: TODO
 * @date 2023年11月23日
 * @version: 1.0
 */
@Component
@Slf4j
@EnableScheduling
public class PullTrack123Job {
    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    /**
     * @PlatformCategoryType(PlatformCategoryEnum.TMS)
     * @PlatformType(PlatformDictEnum.TRACK123)
     * @BusinessType(BusinessTypeEnum.GET_TRACK)
     * 拉取跟踪单的物流轨迹详情
     */
    @XxlJob("pullTrackNoTrackDetail")
    public void pullTrackNoTrackDetail() {
        threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(PlatformDictEnum.TRACK123.getCode(), true);
        });
    }

    @XxlJob("pullTrackNoTrack")
    public void pullTrackNoTrack() {
        threadPoolTaskExecutor.execute(() -> {
            JobTaskDTO jobTaskDTO = new JobTaskDTO();
            jobTaskDTO.setApiCode("getTrack");
            jobTaskDTO.setPlatformApiId("53");
            jobTaskDTO.setApiName("拉取跟踪单的物流轨迹详情");
            jobTaskDTO.setId("1722211371473559656");
            jobTaskDTO.setIntervalTime(1800);
            jobTaskDTO.setLastTime(LocalDateTime.now());
            jobTaskDTO.setNextTime(LocalDateTime.now());
            jobTaskDTO.setStatus(1);
            jobTaskDTO.setRetryTimes(1);
            jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
            jobTaskDTO.setDictPlatform(PlatformDictEnum.TRACK123.getCode());
            jobTaskDTO.setBillType(BusinessTypeEnum.GET_TRACK.getCode());
            platformDataThread.pullOrder(jobTaskDTO);
        });
    }

    @XxlJob("track123CleanExecute")
    public void track123CleanExecute() {
        List<CleanDataTableEnum> platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.TRACK123.getCode());
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
