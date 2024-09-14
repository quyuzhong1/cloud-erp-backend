package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.OverseasInTransitDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.mrp.vo.OverseasInTransitDetailVO;

import java.util.List;

/**
 * <p>
 * 海外在途明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface OverseasInTransitDetailService extends SuperService<OverseasInTransitDetailEntity> {

    PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 获取海外仓在途明细
     * @param detailId 建议明细
     */
    List<OverseasInTransitDetailEntity> getByReplenishmentId(String detailId);
}
