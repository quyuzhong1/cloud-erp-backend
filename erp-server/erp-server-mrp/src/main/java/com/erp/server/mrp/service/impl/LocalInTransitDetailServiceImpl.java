package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.InventoryDetailTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.LocalInTransitDetailEntity;
import com.erp.model.mrp.vo.LocalInTransitDetailVO;
import com.erp.server.mrp.mapper.LocalInTransitDetailMapper;
import com.erp.server.mrp.service.BillShopInventoryDetailService;
import com.erp.server.mrp.service.LocalInTransitDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 本地在途明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class LocalInTransitDetailServiceImpl extends SuperServiceImpl<LocalInTransitDetailMapper, LocalInTransitDetailEntity> implements LocalInTransitDetailService {
    @Resource
    private BillShopInventoryDetailService billShopInventoryDetailService;

    @Override
    public PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<LocalInTransitDetailVO> page = baseMapper.localInTransitDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        List<String> ids = page.getRecords()
                .stream()
                .map(LocalInTransitDetailVO::getId)
                .collect(Collectors.toList());
        Map<String, Integer> shopInventoryMap = billShopInventoryDetailService.listByMainIdsAndShopId(ids, params.getParams().getShopId());
        for (LocalInTransitDetailVO vo : page.getRecords()) {
            vo.setShopInTransitQty(shopInventoryMap.get(vo.getId()));
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<LocalInTransitDetailEntity> getByReplenishmentId(String detailId) {
        return list(Wrappers.<LocalInTransitDetailEntity>lambdaQuery().eq(LocalInTransitDetailEntity::getReplenishmentDetailId, detailId));
    }

    @Override
    public int totalQtyByReplenishment(String detailId) {
        return getByReplenishmentId(detailId).stream()
                .map(LocalInTransitDetailEntity::getQty)
                .reduce(0, Math::addExact);
    }

    @Override
    public int totalQtyByReplenishmentAndSourceType(InventoryDetailTotalDTO params) {
        List<LocalInTransitDetailEntity> list = list(Wrappers.<LocalInTransitDetailEntity>lambdaQuery().eq(LocalInTransitDetailEntity::getReplenishmentDetailId, params.getDetailId())
                .eq(LocalInTransitDetailEntity::getSourceType, params.getSourceType())
        );
        List<String> ids = list.stream().map(LocalInTransitDetailEntity::getId)
                .collect(Collectors.toList());
        return billShopInventoryDetailService.totalByMainIdsAndShopId(ids, params.getShopId());
    }
}
