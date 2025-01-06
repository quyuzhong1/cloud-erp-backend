package com.erp.server.wms.schedule;

import cn.hutool.core.util.StrUtil;
import com.erp.server.wms.service.OrderProcessingService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 订单跟踪任务
 * @author will
 * @date 2024/12/18 18:07
 */
@Component
@Slf4j
@EnableScheduling
public class OrderProcessingJob {
    @Resource
    private OrderProcessingService orderProcessingService;

    /**
     * 自动执行订单跟踪任务
     * @author will
     * @date 2024/12/18 18:08
     * @return ReturnT<String>
     */
    @XxlJob("autoOrderProcessing")
    public ReturnT<String> autoOrderProcessing() {
        XxlJobHelper.log("=====自动执行订单跟踪任务 开始任务=====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        LocalDate startDate = null;
        if (StrUtil.isNotBlank(jobParam)) {
            startDate = LocalDate.parse(jobParam, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        orderProcessingService.autoOrderProcessing(startDate);
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====自动执行订单跟踪任务 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
