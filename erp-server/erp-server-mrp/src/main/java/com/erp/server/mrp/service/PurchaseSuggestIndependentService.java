package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestIndependentDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;

import java.util.List;

/**
 * <p>
 * 建议采购 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface PurchaseSuggestIndependentService extends SuperService<PurchaseSuggestMergeEntity> {
    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:48
     * @param params
     * @return List<ListDTO>
     */
    List<PurchaseSuggestIndependentDTO.ListDTO> list(PurchaseSuggestIndependentDTO.ListParamDTO params);

    /**
     * 采购建议导出数据查询
     * @author will
     * @date 2024/9/6 14:54
     * @param dto
     * @return PagingVO<PurchaseSuggestionDTO>
     */
    PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 列表查询
     * @author will
     * @date 2024/10/16 10:22
     * @param dto 
     * @return PagingVO<ListDTO>
     */
    PagingVO<PurchaseSuggestIndependentDTO.ListDTO> paging(PagingDTO<PurchaseSuggestIndependentDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/10/16 15:28
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO);
    /**
     * 独立采购弹框
     * @author will
     * @date 2025/1/8 15:49
     * @param id
     * @return List<IndependentFrameDTO>
     */
    List<PurchaseSuggestIndependentDTO.IndependentFrameDTO> viewIndependentFrame( String id);
}
