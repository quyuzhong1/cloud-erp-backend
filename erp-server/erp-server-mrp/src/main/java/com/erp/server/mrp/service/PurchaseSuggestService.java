package com.erp.server.mrp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;

/**
 * <p>
 * 建议采购 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface PurchaseSuggestService extends SuperService<PurchaseSuggestEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    Boolean update(PurchaseSuggestDTO.UpdateDTO dto);

    /**
     * 采购建议导出数据查询
     * @author will
     * @date 2024/9/6 14:54
     * @param dto
     * @return PagingVO<PurchaseSuggestionDTO>
     */
    PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
}
