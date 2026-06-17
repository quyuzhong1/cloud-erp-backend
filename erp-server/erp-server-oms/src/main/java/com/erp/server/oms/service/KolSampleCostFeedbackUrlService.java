package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.erp.model.oms.entity.KolSampleCostFeedbackUrlEntity;

import java.util.List;

/**
 * <p>
 * 寄样费用回片链接 服务类
 * </p>
 *
 * @author codex
 * @since 2026-05-08
 */
public interface KolSampleCostFeedbackUrlService extends SuperService<KolSampleCostFeedbackUrlEntity> {

    /**
     * 按回片登记同步寄样费用回片链接。
     *
     * @param feedback 回片登记
     */
    void syncByFeedback(KolFeedbackEntity feedback);

    /**
     * 批量按回片登记同步寄样费用回片链接，供导入等批量场景减少逐条查询。
     *
     * @param feedbackList 回片登记列表
     */
    void syncByFeedbackList(List<KolFeedbackEntity> feedbackList);

    /**
     * 按回片登记删除寄样费用回片链接。
     *
     * @param feedback 回片登记
     * @param hasSameActiveFeedback 是否仍存在相同来源明细和链接的有效回片登记
     */
    void removeByFeedback(KolFeedbackEntity feedback, boolean hasSameActiveFeedback);

    /**
     * 按链接哈希同步回片状态。
     *
     * @param urlHash 回片链接哈希
     * @param feedbackStatus 回片状态
     */
    void syncFeedbackStatusByUrlHash(String urlHash, String feedbackStatus);
}
