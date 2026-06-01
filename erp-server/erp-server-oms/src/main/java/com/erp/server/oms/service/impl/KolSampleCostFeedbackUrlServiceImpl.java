package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.erp.model.oms.entity.KolSampleCostFeedbackUrlEntity;
import com.erp.server.oms.mapper.KolSampleCostFeedbackUrlMapper;
import com.erp.server.oms.service.KolSampleCostFeedbackUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 寄样费用回片链接 服务实现类
 * </p>
 *
 * @author codex
 * @since 2026-05-08
 */
@Service
public class KolSampleCostFeedbackUrlServiceImpl extends SuperServiceImpl<KolSampleCostFeedbackUrlMapper, KolSampleCostFeedbackUrlEntity>
        implements KolSampleCostFeedbackUrlService {

    private static final List<String> SAMPLE_SOURCE_TYPES = Arrays.asList(
            SourceTypeEnum.KOL_B2B_APPLICATION.getCode(),
            SourceTypeEnum.KOL_B2C_APPLICATION.getCode());

    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "feedback.sourceType,feedback.sourceDetailId,feedback.urlHash")
    @Override
    public void syncByFeedback(KolFeedbackEntity feedback) {
        if (!isValidSampleFeedback(feedback)) {
            return;
        }
        String url = CharSequenceUtil.trim(feedback.getUrl());
        KolSampleCostFeedbackUrlEntity existEntity = lambdaQuery()
                .eq(KolSampleCostFeedbackUrlEntity::getSourceType, feedback.getSourceType())
                .eq(KolSampleCostFeedbackUrlEntity::getSourceDetailId, feedback.getSourceDetailId())
                .eq(KolSampleCostFeedbackUrlEntity::getUrlHash, feedback.getUrlHash())
                .eq(KolSampleCostFeedbackUrlEntity::getIsDeleted, false)
                .one();
        if (existEntity != null) {
            updateExistingFeedbackUrl(existEntity, feedback, url);
            return;
        }

        KolSampleCostFeedbackUrlEntity entity = buildEntity(feedback, url);
        entity.setSort(nextSort(feedback.getSourceType(), feedback.getSourceDetailId()));
        save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByFeedback(KolFeedbackEntity feedback, boolean hasSameActiveFeedback) {
        if (!isValidSampleFeedback(feedback) || hasSameActiveFeedback) {
            return;
        }
        lambdaUpdate()
                .set(KolSampleCostFeedbackUrlEntity::getIsDeleted, true)
                .eq(KolSampleCostFeedbackUrlEntity::getSourceType, feedback.getSourceType())
                .eq(KolSampleCostFeedbackUrlEntity::getSourceDetailId, feedback.getSourceDetailId())
                .eq(KolSampleCostFeedbackUrlEntity::getUrlHash, feedback.getUrlHash())
                .eq(KolSampleCostFeedbackUrlEntity::getIsDeleted, false)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncFeedbackStatusByUrlHash(String urlHash, String feedbackStatus) {
        if (CharSequenceUtil.isBlank(urlHash)) {
            return;
        }
        lambdaUpdate()
                .set(KolSampleCostFeedbackUrlEntity::getFeedbackStatus, feedbackStatus)
                .eq(KolSampleCostFeedbackUrlEntity::getUrlHash, urlHash)
                .eq(KolSampleCostFeedbackUrlEntity::getIsDeleted, false)
                .update();
    }

    private KolSampleCostFeedbackUrlEntity buildEntity(KolFeedbackEntity feedback, String url) {
        return new KolSampleCostFeedbackUrlEntity()
                .setSourceType(feedback.getSourceType())
                .setSourceId(CharSequenceUtil.blankToDefault(feedback.getSourceId(), ""))
                .setSourceCode(CharSequenceUtil.blankToDefault(feedback.getSourceCode(), ""))
                .setSourceDetailId(feedback.getSourceDetailId())
                .setFeedbackId(feedback.getId())
                .setPartnerId(CharSequenceUtil.blankToDefault(feedback.getPartnerId(), ""))
                .setPartnerNickname(CharSequenceUtil.blankToDefault(feedback.getPartnerNickname(), ""))
                .setUrl(url)
                .setUrlHash(feedback.getUrlHash())
                .setFeedbackStatus(CharSequenceUtil.blankToDefault(feedback.getFeedbackStatus(), ""));
    }

    private void updateExistingFeedbackUrl(KolSampleCostFeedbackUrlEntity existEntity, KolFeedbackEntity feedback, String url) {
        KolSampleCostFeedbackUrlEntity updateEntity = buildEntity(feedback, url);
        updateEntity.setId(existEntity.getId());
        updateEntity.setSort(existEntity.getSort());
        updateEntity.setVersion(existEntity.getVersion());
        updateById(updateEntity);
    }

    private Integer nextSort(String sourceType, String sourceDetailId) {
        List<KolSampleCostFeedbackUrlEntity> list = lambdaQuery()
                .eq(KolSampleCostFeedbackUrlEntity::getSourceType, sourceType)
                .eq(KolSampleCostFeedbackUrlEntity::getSourceDetailId, sourceDetailId)
                .eq(KolSampleCostFeedbackUrlEntity::getIsDeleted, false)
                .orderByDesc(KolSampleCostFeedbackUrlEntity::getSort)
                .last("limit 1")
                .list();
        if (CollUtil.isEmpty(list) || list.get(0).getSort() == null) {
            return 1;
        }
        return list.get(0).getSort() + 1;
    }

    private boolean isValidSampleFeedback(KolFeedbackEntity feedback) {
        return feedback != null
                && SAMPLE_SOURCE_TYPES.contains(feedback.getSourceType())
                && CharSequenceUtil.isNotBlank(feedback.getSourceDetailId())
                && CharSequenceUtil.isNotBlank(feedback.getUrl())
                && CharSequenceUtil.isNotBlank(feedback.getUrlHash());
    }
}
