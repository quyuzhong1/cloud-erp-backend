package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;

import java.util.Collection;
import java.util.List;

public interface DmpLogisticsTrackWebhookRecordService extends SuperService<DmpLogisticsTrackWebhookRecordEntity> {

    DmpLogisticsTrackWebhookRecordEntity saveKuaidi100RawRecord(String param, String sign);

    DmpLogisticsTrackWebhookRecordEntity saveTrack123RawRecord(String rawData);

    void prepareRecords(String platformCode, int timeoutMinutes);

    List<DmpLogisticsTrackWebhookRecordEntity> claimLatestWaitRecords(String platformCode, int limit);

    void markFinish(String platformCode, Collection<String> ids);

    void markError(String platformCode, String id, String reason);
}
