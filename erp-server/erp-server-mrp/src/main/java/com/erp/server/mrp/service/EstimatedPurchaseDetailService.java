package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedPurchaseDetailEntity;
import com.erp.model.mrp.vo.EstimatedPurchaseVO;

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
}
