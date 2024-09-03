package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.FbaInTransitDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.mrp.vo.FbaInTransitDetailVO;

/**
 * <p>
 * fba在途明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface FbaInTransitDetailService extends SuperService<FbaInTransitDetailEntity> {

    PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
}
