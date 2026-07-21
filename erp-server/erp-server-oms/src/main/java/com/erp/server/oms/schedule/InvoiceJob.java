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
 * <p>
 * InvoiceJob 任务参数示例：
 * {"queryHistory":false,"useReceiverWhenNoBillDetail":true}
 * useReceiverWhenNoBillDetail=true 时，无 dmp_so_bill_detail 则回退订单买家信息生成 PDF；两者都没有才跳过
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
