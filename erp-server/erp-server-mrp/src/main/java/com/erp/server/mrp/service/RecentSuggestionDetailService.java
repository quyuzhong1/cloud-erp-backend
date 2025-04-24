package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.RecentSuggestionDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 最近建议明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-30
 */
public interface RecentSuggestionDetailService extends SuperService<RecentSuggestionDetailEntity> {

    List<RecentSuggestionDetailEntity> listByReplenishmentDetailIds(List<String> detailIds);
}
