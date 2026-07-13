package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.common.business.dto.WebhookResult;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Track123WebhookHandler implements WebhookHandler {

    private DmpLogisticsTrackWebhookRecordService webhookRecordService;

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        // Track123 active pull has no verify logic, so webhook keeps the same behavior.
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        log.warn("webhook获取Track123数据,{}", data);
        getWebhookRecordService().saveTrack123RawRecord(data);
        return WebhookResult.isSuccess();
    }

    private DmpLogisticsTrackWebhookRecordService getWebhookRecordService() {
        if (webhookRecordService == null) {
            webhookRecordService = SpringUtil.getBean(DmpLogisticsTrackWebhookRecordService.class);
        }
        return webhookRecordService;
    }
}
