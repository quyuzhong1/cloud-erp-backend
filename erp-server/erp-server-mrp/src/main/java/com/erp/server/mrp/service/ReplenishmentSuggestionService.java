package com.erp.server.mrp.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.vo.*;

import java.util.List;

/**
 * <p>
 * 补货建议主表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface ReplenishmentSuggestionService extends SuperService<ReplenishmentSuggestionEntity> {

    PagingVO<ReplenishmentSuggestionVO.PagingView> paging(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params);

    ReplenishmentSuggestionVO.View view(String detailId);

    PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<String> params);

    PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<String> params);

    PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<LocalInTransitDetailDTO> params);

    PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<EstimatedDeliveryDTO> params);

    PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<EstimatedPurchaseDTO> params);

    Integer inventoryTotal(InventoryTotalDTO params);

    InventoryDetailVO inventoryDetail(InventoryTotalDTO params);
    /**
     * 暂不补货
     * @author will
     * @date 2024/8/29 15:01
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO notRestockingReplenishment(String id,String replenishmentRemark);
    /**
     * 恢复补货
     * @author will
     * @date 2024/8/29 15:02
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO restoreReplenishment(String id,String replenishmentRemark);
    /**
     * 单个设置规则
     * @author will
     * @date 2024/8/29 15:57
     * @param dto
     */
    void updateRule(ReplenishmentSuggestionDTO.UpdateRuleDTO dto);
    /**
     * 批量设置规则
     * @author will
     * @date 2024/8/29 15:57
     * @param id
     * @param stockUpUpdateDTO
     * @param salesQtyUpdateDTO
     * @return BatchResultDTO
     */
    BatchResultDTO batchUpdateRule(String id, CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO, CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO);
    /**
     * 恢复规则设置
     * @author will
     * @date 2024/8/29 15:57
     * @param id
     * @param ruleTypeList
     * @return BatchResultDTO
     */
    BatchResultDTO restoreRule(String id, List<String> ruleTypeList);
}
