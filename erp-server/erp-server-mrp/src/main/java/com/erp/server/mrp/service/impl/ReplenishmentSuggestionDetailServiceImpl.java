package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.ReplenishmentSuggestionDetailEntity;
import com.erp.server.mrp.mapper.ReplenishmentSuggestionDetailMapper;
import com.erp.server.mrp.service.ReplenishmentSuggestionDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 补货建议详细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class ReplenishmentSuggestionDetailServiceImpl extends SuperServiceImpl<ReplenishmentSuggestionDetailMapper, ReplenishmentSuggestionDetailEntity> implements ReplenishmentSuggestionDetailService {

    @Override
    public List<ReplenishmentSuggestionDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(ReplenishmentSuggestionDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public ReplenishmentSuggestionDetailEntity getByMainId(String suggestId) {
        return getOne(Wrappers.<ReplenishmentSuggestionDetailEntity>lambdaQuery()
                .eq(ReplenishmentSuggestionDetailEntity::getMainId, suggestId)
                .orderByDesc(ReplenishmentSuggestionDetailEntity::getCreateTime)
                .last("LIMIT 1"));
    }

}
