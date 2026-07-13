package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;

import java.util.Collection;
import java.util.List;

public interface DmpLogisticsTrackWebhookRecordService extends SuperService<DmpLogisticsTrackWebhookRecordEntity> {

    DmpLogisticsTrackWebhookRecordEntity saveKuaidi100RawRecord(String param, String sign);

    void prepareKuaidi100Records(int timeoutMinutes);

    List<DmpLogisticsTrackWebhookRecordEntity> claimKuaidi100LatestWaitRecords(int limit);

    void markFinish(Collection<String> ids);

    void markError(String id, String reason);
}
