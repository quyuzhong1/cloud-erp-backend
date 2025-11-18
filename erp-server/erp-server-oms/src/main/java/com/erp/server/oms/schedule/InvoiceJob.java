package com.erp.server.oms.schedule;

import com.erp.server.oms.service.InvoiceInfoService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 发票重试job
 */
@Component
@Slf4j
public class InvoiceJob {

    @Resource
    private InvoiceInfoService invoiceInfoService;

    @XxlJob("InvoiceJob")
    public ReturnT<String> invoiceJob() {
        String jobParam = XxlJobHelper.getJobParam();
        invoiceInfoService.retryInvoice(jobParam);
        return ReturnT.SUCCESS;
    }

    @XxlJob("HandleUploadingJob")
    public ReturnT<String> HandleUploadingJob() throws Exception {
        invoiceInfoService.queryUploadingInvoice();
        return ReturnT.SUCCESS;
    }

}
