package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.LocalInTransitDetailEntity;
import com.erp.model.mrp.vo.LocalInTransitDetailVO;

import java.util.List;

/**
 * <p>
 * 本地在途明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface LocalInTransitDetailService extends SuperService<LocalInTransitDetailEntity> {

    PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 获取本地在途明细
     * @param detailId 建议id
     */
    List<LocalInTransitDetailEntity> getByReplenishmentId(String detailId);

    /**
     * 总数量
     * @param detailId 明细id
     */
    int totalQtyByReplenishment(String detailId);
}
