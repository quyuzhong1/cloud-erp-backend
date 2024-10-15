package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedPurchaseDetailEntity;
import com.erp.model.mrp.vo.EstimatedPurchaseVO;

import java.util.List;

/**
 * <p>
 * 预计采购明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface EstimatedPurchaseDetailService extends SuperService<EstimatedPurchaseDetailEntity> {

    PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 获取预计采购
     * @param detailId 明细id
     */
    List<EstimatedPurchaseDetailEntity> getByReplenishmentId(String detailId);

    /**
     * 总数量
     * @param detailId 明细id
     */
    int totalQtyByReplenishment(String detailId);
}
