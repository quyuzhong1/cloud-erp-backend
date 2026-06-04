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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final String FEEDBACK_URL_LOCK_BUSINESS_TYPE = "kolSampleCostFeedbackUrl:syncByFeedback";
    private static final int FEEDBACK_URL_QUERY_BATCH_SIZE = 100;

    private static final List<String> SAMPLE_SOURCE_TYPES = Arrays.asList(
            SourceTypeEnum.KOL_B2B_APPLICATION.getCode(),
            SourceTypeEnum.KOL_B2C_APPLICATION.getCode());

    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "feedback.sourceType,feedback.sourceDetailId", businessType = FEEDBACK_URL_LOCK_BUSINESS_TYPE, unlockAfterTx = true)
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
    // DistributeLockerAspect#getValuesByParam 会展开 List 参数，单条和批量最终锁 key 都是 sourceType|sourceDetailId。
    @DistributeLocker(keyName = "feedbackList.sourceType,feedbackList.sourceDetailId", businessType = FEEDBACK_URL_LOCK_BUSINESS_TYPE, unlockAfterTx = true)
    @Override
    public void syncByFeedbackList(List<KolFeedbackEntity> feedbackList) {
        if (CollUtil.isEmpty(feedbackList)) {
            return;
        }
        List<KolFeedbackEntity> validFeedbackList = feedbackList.stream()
                .filter(this::isValidSampleFeedback)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validFeedbackList)) {
            return;
        }

        Set<String> urlHashSet = validFeedbackList.stream()
                .map(KolFeedbackEntity::getUrlHash)
                .collect(Collectors.toSet());

        List<KolSampleCostFeedbackUrlEntity> activeUrlList = listActiveFeedbackUrlsByBatch(validFeedbackList);
        Map<String, KolSampleCostFeedbackUrlEntity> existUrlMap = activeUrlList.stream()
                .filter(item -> urlHashSet.contains(item.getUrlHash()))
                .collect(Collectors.toMap(this::buildUrlKey, item -> item, (v1, v2) -> v1));
        Map<String, Integer> maxSortMap = activeUrlList.stream()
                .filter(item -> Objects.nonNull(item.getSort()))
                .collect(Collectors.groupingBy(
                        this::buildSourceDetailKey,
                        Collectors.collectingAndThen(Collectors.maxBy((v1, v2) -> v1.getSort().compareTo(v2.getSort())),
                                item -> item.map(KolSampleCostFeedbackUrlEntity::getSort).orElse(0))));

        List<KolSampleCostFeedbackUrlEntity> addList = new ArrayList<>();
        List<KolSampleCostFeedbackUrlEntity> updateList = new ArrayList<>();
        Map<String, KolSampleCostFeedbackUrlEntity> pendingAddMap = new HashMap<>();
        for (KolFeedbackEntity feedback : validFeedbackList) {
            String url = CharSequenceUtil.trim(feedback.getUrl());
            String urlKey = buildUrlKey(feedback.getSourceType(), feedback.getSourceDetailId(), feedback.getUrlHash());
            KolSampleCostFeedbackUrlEntity existEntity = existUrlMap.get(urlKey);
            if (Objects.nonNull(existEntity)) {
                KolSampleCostFeedbackUrlEntity updateEntity = buildEntity(feedback, url);
                updateEntity.setId(existEntity.getId());
                updateEntity.setSort(existEntity.getSort());
                updateEntity.setVersion(existEntity.getVersion());
                updateList.add(updateEntity);
                continue;
            }
            KolSampleCostFeedbackUrlEntity pendingAddEntity = pendingAddMap.get(urlKey);
            if (Objects.nonNull(pendingAddEntity)) {
                KolSampleCostFeedbackUrlEntity latestEntity = buildEntity(feedback, url);
                latestEntity.setSort(pendingAddEntity.getSort());
                addList.remove(pendingAddEntity);
                addList.add(latestEntity);
                pendingAddMap.put(urlKey, latestEntity);
                continue;
            }

            String sourceDetailKey = buildSourceDetailKey(feedback.getSourceType(), feedback.getSourceDetailId());
            Integer nextSort = maxSortMap.getOrDefault(sourceDetailKey, 0) + 1;
            maxSortMap.put(sourceDetailKey, nextSort);
            KolSampleCostFeedbackUrlEntity addEntity = buildEntity(feedback, url);
            addEntity.setSort(nextSort);
            addList.add(addEntity);
            pendingAddMap.put(urlKey, addEntity);
        }
        if (CollUtil.isNotEmpty(addList)) {
            saveBatch(addList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            updateBatchById(updateList);
        }
    }

    private List<KolSampleCostFeedbackUrlEntity> listActiveFeedbackUrlsByBatch(List<KolFeedbackEntity> validFeedbackList) {
        Map<String, KolSampleCostFeedbackUrlEntity> activeUrlMap = new HashMap<>();
        for (int start = 0; start < validFeedbackList.size(); start += FEEDBACK_URL_QUERY_BATCH_SIZE) {
            List<KolFeedbackEntity> partitionList = validFeedbackList.subList(start,
                    Math.min(start + FEEDBACK_URL_QUERY_BATCH_SIZE, validFeedbackList.size()));
            Set<String> sourceTypeSet = partitionList.stream()
                    .map(KolFeedbackEntity::getSourceType)
                    .collect(Collectors.toSet());
            Set<String> sourceDetailIdSet = partitionList.stream()
                    .map(KolFeedbackEntity::getSourceDetailId)
                    .collect(Collectors.toSet());
            if (CollUtil.isEmpty(sourceTypeSet) || CollUtil.isEmpty(sourceDetailIdSet)) {
                continue;
            }
            List<KolSampleCostFeedbackUrlEntity> partitionActiveUrlList = lambdaQuery()
                    .in(KolSampleCostFeedbackUrlEntity::getSourceType, sourceTypeSet)
                    .in(KolSampleCostFeedbackUrlEntity::getSourceDetailId, sourceDetailIdSet)
                    .eq(KolSampleCostFeedbackUrlEntity::getIsDeleted, false)
                    .list();
            for (KolSampleCostFeedbackUrlEntity entity : partitionActiveUrlList) {
                activeUrlMap.putIfAbsent(CharSequenceUtil.blankToDefault(entity.getId(), buildUrlKey(entity)), entity);
            }
        }
        return new ArrayList<>(activeUrlMap.values());
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

    private String buildUrlKey(KolSampleCostFeedbackUrlEntity entity) {
        return buildUrlKey(entity.getSourceType(), entity.getSourceDetailId(), entity.getUrlHash());
    }

    private String buildUrlKey(String sourceType, String sourceDetailId, String urlHash) {
        return sourceType + "#" + sourceDetailId + "#" + urlHash;
    }

    private String buildSourceDetailKey(KolSampleCostFeedbackUrlEntity entity) {
        return buildSourceDetailKey(entity.getSourceType(), entity.getSourceDetailId());
    }

    private String buildSourceDetailKey(String sourceType, String sourceDetailId) {
        return sourceType + "#" + sourceDetailId;
    }

    private boolean isValidSampleFeedback(KolFeedbackEntity feedback) {
        return feedback != null
                && SAMPLE_SOURCE_TYPES.contains(feedback.getSourceType())
                && CharSequenceUtil.isNotBlank(feedback.getSourceDetailId())
                && CharSequenceUtil.isNotBlank(feedback.getUrl())
                && CharSequenceUtil.isNotBlank(feedback.getUrlHash());
    }
}
