package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedDeliveryDetailEntity;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.mrp.vo.EstimatedDeliveryVO;
import com.erp.server.mrp.mapper.EstimatedDeliveryDetailMapper;
import com.erp.server.mrp.service.EstimatedDeliveryDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 预计发货明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class EstimatedDeliveryDetailServiceImpl extends SuperServiceImpl<EstimatedDeliveryDetailMapper, EstimatedDeliveryDetailEntity> implements EstimatedDeliveryDetailService {

    @Override
    public PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<EstimatedDeliveryVO> page = baseMapper.estimatedDelivery(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        for (EstimatedDeliveryVO vo : page.getRecords()) {
            if (SourceTypeEnum.REPLENISHMENT_PLAN.getCode().equals(vo.getSourceCode())) {
                vo.setStatusName(SuggestStatusEnum.getName(vo.getStatus()));
            } else {
                vo.setStatusName(ApproveStatusEnum.getName(vo.getStatus()));
            }
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<EstimatedDeliveryDetailEntity> getByReplenishmentIdAndType(String detailId, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum) {
        return list(Wrappers.<EstimatedDeliveryDetailEntity>lambdaQuery()
                .eq(EstimatedDeliveryDetailEntity::getReplenishmentDetailId, detailId)
                .eq(EstimatedDeliveryDetailEntity::getType, replenishmentInventoryTypeEnum.getCode())
        );
    }

    @Override
    public int totalQtyByReplenishment(String detailId, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum) {
        return getByReplenishmentIdAndType(detailId, replenishmentInventoryTypeEnum).stream()
                .map(EstimatedDeliveryDetailEntity::getQty)
                .reduce(0, Math::addExact);
    }
}
