package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.RecentSuggestionDetailEntity;
import com.erp.server.mrp.mapper.RecentSuggestionDetailMapper;
import com.erp.server.mrp.service.RecentSuggestionDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 最近建议明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-30
 */
@Service
public class RecentSuggestionDetailServiceImpl extends SuperServiceImpl<RecentSuggestionDetailMapper, RecentSuggestionDetailEntity> implements RecentSuggestionDetailService {

    @Override
    public List<RecentSuggestionDetailEntity> listByReplenishmentDetailIds(List<String> detailIds) {
        return list(Wrappers.<RecentSuggestionDetailEntity>lambdaQuery().in(RecentSuggestionDetailEntity::getReplenishmentDetailId, detailIds));
    }
}
