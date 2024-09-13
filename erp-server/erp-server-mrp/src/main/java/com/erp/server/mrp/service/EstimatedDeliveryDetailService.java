package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedDeliveryDetailEntity;
import com.erp.model.mrp.vo.EstimatedDeliveryVO;

/**
 * <p>
 * 预计发货明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface EstimatedDeliveryDetailService extends SuperService<EstimatedDeliveryDetailEntity> {

    PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
}
