package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.ReplenishmentSuggestionFavoriteDTO;
import com.erp.model.mrp.entity.ReplenishmentSuggestionFavoriteEntity;
import com.erp.server.mrp.mapper.ReplenishmentSuggestionFavoriteMapper;
import com.erp.server.mrp.service.ReplenishmentSuggestionFavoriteService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 补货建议关注表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@Service
public class ReplenishmentSuggestionFavoriteServiceImpl extends SuperServiceImpl<ReplenishmentSuggestionFavoriteMapper, ReplenishmentSuggestionFavoriteEntity> implements ReplenishmentSuggestionFavoriteService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReplenishmentSuggestionFavoriteDTO.AddDTO addDTO) {
        ReplenishmentSuggestionFavoriteEntity replenishmentSuggestionFavoriteEntity = new ReplenishmentSuggestionFavoriteEntity();
        BeanMapperUtils.copy(addDTO, replenishmentSuggestionFavoriteEntity);

        // 数据处理
        handleData(replenishmentSuggestionFavoriteEntity);

        log.info("开始新增补货建议关注单");
        boolean save = super.save(replenishmentSuggestionFavoriteEntity);
        if(!save) {
            throw new ServiceException("补货建议关注单保存失败");
        }
        return new BaseResultDTO.AddDTO(replenishmentSuggestionFavoriteEntity.getId(), replenishmentSuggestionFavoriteEntity.getId());
    }

    @Override
    public Boolean isFavorite(String userId, String replenishmentSuggestionId) {
        ReplenishmentSuggestionFavoriteEntity entity = lambdaQuery().eq(ReplenishmentSuggestionFavoriteEntity::getUserId, userId)
                .eq(ReplenishmentSuggestionFavoriteEntity::getReplenishmentSuggestionId, replenishmentSuggestionId)
                .last("limit 1")
                .one();
        return ObjectUtil.isEmpty(entity) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public void cancelFavorite(String userId, String replenishmentSuggestionId) {
        lambdaUpdate().eq(ReplenishmentSuggestionFavoriteEntity::getUserId,userId)
                .eq(ReplenishmentSuggestionFavoriteEntity::getReplenishmentSuggestionId,replenishmentSuggestionId)
                .remove();
    }

    @Override
    public List<ReplenishmentSuggestionFavoriteEntity> listByReplenishmentIds(List<String> ids) {
        return list(Wrappers.<ReplenishmentSuggestionFavoriteEntity>lambdaQuery().in(ReplenishmentSuggestionFavoriteEntity::getReplenishmentSuggestionId, ids));
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ReplenishmentSuggestionFavoriteEntity entity) {
        Boolean favorite = isFavorite(entity.getUserId(), entity.getReplenishmentSuggestionId());
        if (Boolean.TRUE.equals(favorite)) {
            throw new ServiceException("补货建议已关注，无需再次关注");
        }
    }
}
