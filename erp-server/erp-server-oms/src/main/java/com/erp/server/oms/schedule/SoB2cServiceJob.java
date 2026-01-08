package com.erp.server.oms.schedule;

import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoReceiptService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: wtr
 * @Date: 2026/1/7 12:00
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class SoB2cServiceJob {

    @Resource
    private SoB2cService soB2cService;

    @XxlJob("deleteB2cSoJob")
    public ReturnT<String> deleteB2cSoJob() {
        XxlJobHelper.log("开始定时删除30天以前的b2c销售订单...");
        String jobParam = XxlJobHelper.getJobParam();
        soB2cService.deleteB2cSoJob();
        XxlJobHelper.log("定时删除30天以前的b2c销售订单结束...");
        return ReturnT.SUCCESS;
    }
}
