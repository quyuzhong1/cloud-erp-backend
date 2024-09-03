package com.erp.server.mrp.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.vo.*;

import java.math.BigDecimal;
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
    /**
     * 补货建议列表
     * @param params 参数
     */
    PagingVO<ReplenishmentSuggestionVO.PagingView> paging(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params);
    /**
     * 补货建议明细
     * @param detailId 明细id
     */
    ReplenishmentSuggestionVO.View view(String detailId);
    /**
     * fba在途明细明细
     * @param params 明细id
     */
    PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<String> params);
    /**
     * 海外仓在途明细
     * @param params 明细id
     */
    PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<String> params);
    /**
     * 本地仓在途明细
     * @param params 明细id
     */
    PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<LocalInTransitDetailDTO> params);
    /**
     * 预计发货明细
     * @param params 明细id
     */
    PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<EstimatedDeliveryDTO> params);
    /**
     * 预计采购明细
     * @param params 明细id
     */
    PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<EstimatedPurchaseDTO> params);
    /**
     * 店铺库存明细
     * @param params 明细id
     */
    InventoryDetailVO inventoryDetail(InventoryTotalDTO params);
    /**
     * 销量分析
     * @param dto 参数
     */
    SalesAnalysisVO salesAnalysis(SalesAnalysisDTO dto);
    /**
     * 历史库存
     * @param dto 参数
     */
    HistoryInventoryVO historyInventory(HistoryInventoryDTO dto);
    /**
     * 断货报告
     */
    List<RptOutOfStockVO> outOfStockReport(String detailId);
    /**
     * 断货报告数量
     */
    BigDecimal outOfStockReportTotal(String detailId);
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
    /**
     * 关注补货建议
     * @author will
     * @date 2024/8/30 10:54
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO favorite(String id);
    /**
     * 取消关注
     * @author will
     * @date 2024/8/30 11:09
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelFavorite(String id);
    /**
     * 添加标签
     * @author will
     * @date 2024/8/30 14:51
     * @param updateLabelDTO
     * @return BatchResultDTO
     */
    BatchResultDTO updateLabel(ReplenishmentSuggestionDTO.UpdateLabelDTO updateLabelDTO);
    /**
     * 批量更新标签
     * @author will
     * @date 2024/8/30 16:02
     * @param id
     * @param labelIdList
     * @return BatchResultDTO
     */
    BatchResultDTO batchAddLabel(String id, List<String> labelIdList);
    /**
     * 取消标签
     * @author will
     * @date 2024/8/30 16:15
     * @param id
     * @param labelIdList
     * @return BatchResultDTO
     */
    BatchResultDTO cancelLabel(String id, List<String> labelIdList);

    /**
     * 获取所有sku和店铺
     */
    List<ReplenishmentSuggestionEntity> listAllSkuAndShop();
}
