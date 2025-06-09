package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.FbaInTransitDetailEntity;
import com.erp.model.mrp.vo.FbaInTransitDetailVO;

import java.util.List;

/**
 * <p>
 * fba在途明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface FbaInTransitDetailService extends SuperService<FbaInTransitDetailEntity> {

    /**
     * fba在途明细
     */
    PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 根据建议
     * @param detailId 建议明细id
     */
    List<FbaInTransitDetailEntity> getByReplenishmentId(String detailId);

    /**
     * fba在途总数量
     * @param detailId id
     */
    int totalQtyByReplenishment(String detailId);
}
