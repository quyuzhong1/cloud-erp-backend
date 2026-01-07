package com.erp.server.oms.schedule;

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
public class SoReceiptJob {

    @Resource
    private SoReceiptService soReceiptService;

    @XxlJob("deleteReceiptJob")
    public ReturnT<String> deleteReceiptJob() {
        XxlJobHelper.log("定时删除30天以前的收款单开始...");
        String jobParam = XxlJobHelper.getJobParam();
        soReceiptService.deleteReceiptJob();
        XxlJobHelper.log("定时删除30天以前的收款单结束...");
        return ReturnT.SUCCESS;
    }
}
