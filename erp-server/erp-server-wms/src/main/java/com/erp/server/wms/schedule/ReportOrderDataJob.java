package com.erp.server.wms.schedule;

import com.erp.server.wms.service.ReportOrderDataService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 报表数据定时任务
 * @author will
 * @date 2024/9/26 14:10
 */
@Component
@Slf4j
public class ReportOrderDataJob {

    @Resource
    private ReportOrderDataService reportOrderDataService;


    /**
     * 自动执行生成报表数据
     * @author will
     * @date 2024/9/25 16:04
     * @return ReturnT<String>
     */
    @XxlJob("generateReportOrderData")
    public ReturnT<String> generateReportOrderData() {
        //可输入时间
        String time = XxlJobHelper.getJobParam();
        XxlJobHelper.log("====开始生成报表数据=====");
        reportOrderDataService.generateReportOrderData();
        XxlJobHelper.log("====开始生成报表数据=====");
        return ReturnT.SUCCESS;
    }

}