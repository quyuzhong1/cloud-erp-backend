package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.ReplenishmentSuggestionFavoriteEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.ReplenishmentSuggestionFavoriteDTO;

/**
 * <p>
 * 补货建议关注表 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
public interface ReplenishmentSuggestionFavoriteService extends SuperService<ReplenishmentSuggestionFavoriteEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReplenishmentSuggestionFavoriteDTO.AddDTO dto);
    /**
     * 是否关注
     * @author will
     * @date 2024/8/30 11:01
     * @param userId
     * @param replenishmentSuggestionId
     * @return Boolean
     */
    Boolean isFavorite(String userId,String replenishmentSuggestionId);
    /**
     * 取消关注
     * @author will
     * @date 2024/8/30 11:12
     * @param userId
     * @param replenishmentSuggestionId
     */
    void cancelFavorite(String userId, String replenishmentSuggestionId);
}
