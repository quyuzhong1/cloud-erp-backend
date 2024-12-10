package com.erp.server.wms.schedule;

import com.erp.server.wms.service.WaveListService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * 波次列表波次状态自动变更
 * @Author jack
 * @Date 2024/10/08
 **/
@Component
@Slf4j
@EnableScheduling
public class WaveListStatusAutoChangeJob {
    @Resource
    private WaveListService waveListService;

    /**
     *
     * @Author jack
     * @Date 2024/10/08
     * @return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("waveListStatusAutoChangeJob")
    public ReturnT<String> poReturnAutoConfirmJob() {
        XxlJobHelper.log("=====波次列表波次状态自动变更 开始任务=====");
        long start = System.currentTimeMillis();
        waveListService.waveListStatusAutoChange("");
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====波次列表波次状态自动变更 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
