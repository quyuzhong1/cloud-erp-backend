package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedDeliveryDetailEntity;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.model.mrp.vo.EstimatedDeliveryVO;

import java.util.List;

/**
 * <p>
 * 预计发货明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface EstimatedDeliveryDetailService extends SuperService<EstimatedDeliveryDetailEntity> {

    /**
     * 获取明细
     */
    PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 获取明细
     * @param detailId 明细id
     * @param replenishmentInventoryTypeEnum 类型
     */
    List<EstimatedDeliveryDetailEntity> getByReplenishmentIdAndType(String detailId, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum);

    /**
     * 预计发货总数量
     * @param detailId 明细id
     * @param replenishmentInventoryTypeEnum 类型
     */
    int totalQtyByReplenishment(String detailId, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum);
}
