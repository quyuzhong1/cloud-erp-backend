package com.erp.server.mrp.schedule;

import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@EnableScheduling
public class DataArchivingJob {

    @Resource
    private DataArchivingService dataArchivingService;

    /**
     * 归档，全量更新数据
     */
    @XxlJob("dataArchiving")
    public ReturnT dataArchiving() {
        XxlJobHelper.log("====开始更新信息====");
        CompletableFuture.runAsync(() -> dataArchivingService.dataArchiving());
        return ReturnT.SUCCESS;
    }
}
