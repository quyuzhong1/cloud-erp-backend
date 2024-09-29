package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.ReplenishmentSuggestionDetailEntity;

import java.util.List;

/**
 * <p>
 * 补货建议详细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface ReplenishmentSuggestionDetailService extends SuperService<ReplenishmentSuggestionDetailEntity> {
    /**
     * 根据主表id集合查询
     * @author will
     * @date 2024/9/4 14:30
     * @param suggestIdList
     * @return List<ReplenishmentSuggestionDetailEntity>
     */
    List<ReplenishmentSuggestionDetailEntity> listByMainIdList(List<String> suggestIdList);



    /**
     * 根据主表id查询明细
     * @date 2024/9/4 14:30
     * @param suggestId
     */
    ReplenishmentSuggestionDetailEntity getByMainId(String suggestId);
}
