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
        invoiceInfoService.retryInvoice();

        return ReturnT.SUCCESS;
    }

    @XxlJob("HandleUploadingJob")
    public ReturnT<String> HandleUploadingJob() throws Exception {
        invoiceInfoService.queryUploadingInvoice();
        return ReturnT.SUCCESS;
    }

    /**
     * 查询上传中的nfe发票
     * @author will
     * @date 2025/4/14 16:04
     * @return ReturnT<String>
     */
    @XxlJob("HandleUploadingNfeJob")
    public ReturnT<String> HandleUploadingNfeJob() {
        XxlJobHelper.log("HandleUploadingNfeJob  执行开始");
        invoiceInfoService.HandleUploadingNfeJob();
        XxlJobHelper.log("HandleUploadingNfeJob  执行结束");
        return ReturnT.SUCCESS;
    }
}
