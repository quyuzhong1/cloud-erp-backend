package com.erp.server.mrp.schedule;

import com.erp.server.mrp.service.DeliverySuggestService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@EnableScheduling
public class DeliverySuggestJob {

    @Resource
    private DeliverySuggestService deliverySuggestService;

    /**
     * 补货计划自动作废
     */
    @XxlJob("deliverySuggestInvalid")
    public ReturnT deliverySuggestInvalid(String param) {
        XxlJobHelper.log("====开始更新信息====");
        deliverySuggestService.deliverySuggestInvalid();
        return ReturnT.SUCCESS;
    }
}
