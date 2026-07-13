package com.erp.server.dmp.service.impl;

import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;
import com.erp.model.dmp.enums.DmpLogisticsTrackWebhookRecordStatusEnum;
import com.erp.server.dmp.handler.Kuaidi100WebhookPayloadParser;
import com.erp.server.dmp.mapper.DmpLogisticsTrackWebhookRecordMapper;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class DmpLogisticsTrackWebhookRecordServiceImpl
        extends SuperServiceImpl<DmpLogisticsTrackWebhookRecordMapper, DmpLogisticsTrackWebhookRecordEntity>
        implements DmpLogisticsTrackWebhookRecordService {

    private static final String PLATFORM_CODE = PlatformDictEnum.KUAIDI100.getCode();

    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    private static final int MAX_CLAIM_LIMIT = 500;

    private static final int MAX_REMARK_LENGTH = 255;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DmpLogisticsTrackWebhookRecordEntity saveKuaidi100RawRecord(String param, String sign) {
        DmpLogisticsTrackWebhookRecordEntity entity = new DmpLogisticsTrackWebhookRecordEntity();
        entity.setPlatformCode(PLATFORM_CODE);
        entity.setTrackNo("");
        entity.setStatus(DmpLogisticsTrackWebhookRecordStatusEnum.WAIT.getCode());
        entity.setRawData(Kuaidi100WebhookPayloadParser.buildRawData(param, sign).toJSONString());
        entity.setRemark("");
        super.save(entity);

        try {
            String trackNo = Kuaidi100WebhookPayloadParser.extractTrackNo(param);
            entity.setTrackNo(trackNo);
            entity.setStatus(DmpLogisticsTrackWebhookRecordStatusEnum.WAIT.getCode());
            entity.setRemark("");
        } catch (Exception e) {
            entity.setStatus(DmpLogisticsTrackWebhookRecordStatusEnum.ERROR.getCode());
            entity.setRemark(limitRemark(e.getMessage()));
        }
        entity.setUpdateTime(LocalDateTime.now());
        super.updateById(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void prepareKuaidi100Records(int timeoutMinutes) {
        int actualTimeoutMinutes = timeoutMinutes > 0 ? timeoutMinutes : DEFAULT_TIMEOUT_MINUTES;
        baseMapper.skipCoveredRecords(PLATFORM_CODE, actualTimeoutMinutes, "");
        baseMapper.recoverTimeoutIngRecords(PLATFORM_CODE, actualTimeoutMinutes, "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<DmpLogisticsTrackWebhookRecordEntity> claimKuaidi100LatestWaitRecords(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        int actualLimit = Math.min(limit, MAX_CLAIM_LIMIT);
        return baseMapper.claimLatestWaitRecords(PLATFORM_CODE, actualLimit);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void markFinish(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        baseMapper.updateStatusByIds(ids,
                PLATFORM_CODE,
                DmpLogisticsTrackWebhookRecordStatusEnum.ING.getCode(),
                DmpLogisticsTrackWebhookRecordStatusEnum.FINISH.getCode(),
                "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void markError(String id, String reason) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        lambdaUpdate().eq(DmpLogisticsTrackWebhookRecordEntity::getId, id)
                .eq(DmpLogisticsTrackWebhookRecordEntity::getPlatformCode, PLATFORM_CODE)
                .eq(DmpLogisticsTrackWebhookRecordEntity::getStatus, DmpLogisticsTrackWebhookRecordStatusEnum.ING.getCode())
                .eq(DmpLogisticsTrackWebhookRecordEntity::getIsDeleted, false)
                .set(DmpLogisticsTrackWebhookRecordEntity::getStatus, DmpLogisticsTrackWebhookRecordStatusEnum.ERROR.getCode())
                .set(DmpLogisticsTrackWebhookRecordEntity::getRemark, limitRemark(reason))
                .set(DmpLogisticsTrackWebhookRecordEntity::getUpdateTime, LocalDateTime.now())
                .update();
    }

    private String limitRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return "";
        }
        return remark.length() > MAX_REMARK_LENGTH ? remark.substring(0, MAX_REMARK_LENGTH) : remark;
    }
}
