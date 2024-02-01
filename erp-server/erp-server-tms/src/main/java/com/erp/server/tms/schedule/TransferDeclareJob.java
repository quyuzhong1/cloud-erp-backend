package com.erp.server.tms.schedule;

import com.erp.server.tms.service.TransferDeclareService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 中转报关定时器
 */
@Component
@Slf4j
@EnableScheduling
public class TransferDeclareJob {

    @Resource
    private TransferDeclareService transferDeclareService;

    /**
     * 报关设置自动生成-定时器调用
     * @return
     */
    @XxlJob("declareAutoGenerationJob")
    public ReturnT declareAutoGenerationJob() {
        XxlJobHelper.log("====开始自动生成报关单====");
        transferDeclareService.declareAutoGenerationJob();
        XxlJobHelper.log("====结束自动生成报关单====");
        return ReturnT.SUCCESS;
    }

    /**
     * 查询平台报关订单信息
     * @return
     */
    @XxlJob("getOrderByCodeJob")
    public ReturnT getOrderByCodeJob() {
        XxlJobHelper.log("====开始查询平台报关订单信息====");
        transferDeclareService.getOrderByCodeJob();
        XxlJobHelper.log("====结束查询平台报关订单信息====");
        return ReturnT.SUCCESS;
    }

}
