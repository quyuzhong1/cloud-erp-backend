package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.InventoryDetailTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.OverseasInTransitDetailEntity;
import com.erp.model.mrp.vo.OverseasInTransitDetailVO;
import com.erp.server.mrp.mapper.OverseasInTransitDetailMapper;
import com.erp.server.mrp.service.OverseasInTransitDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 海外在途明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class OverseasInTransitDetailServiceImpl extends SuperServiceImpl<OverseasInTransitDetailMapper, OverseasInTransitDetailEntity> implements OverseasInTransitDetailService {

    @Override
    public PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<OverseasInTransitDetailVO> page = baseMapper.overseasInTransitDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public List<OverseasInTransitDetailEntity> getByReplenishmentId(String detailId) {
        return list(Wrappers.<OverseasInTransitDetailEntity>lambdaQuery().eq(OverseasInTransitDetailEntity::getReplenishmentDetailId, detailId));
    }

    @Override
    public int totalQtyByReplenishment(String detailId) {
        return getByReplenishmentId(detailId).stream()
                .map(OverseasInTransitDetailEntity::getInTransitQty)
                .reduce(0, Math::addExact);
    }

    @Override
    public int totalQtyByReplenishmentAndSourceType(InventoryDetailTotalDTO params) {
        List<OverseasInTransitDetailEntity> entities = getByReplenishmentId(params.getDetailId());
        return entities.stream()
                .map(OverseasInTransitDetailEntity::getShopPreQty)
                .reduce(0 ,Math::addExact);
    }
}
