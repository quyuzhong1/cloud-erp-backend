package com.erp.server.mrp.service;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.vo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
    /**
     * 海外仓在途明细
     * @param params 明细id
     */
    PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
    /**
     * 本地仓在途明细
     * @param params 明细id
     */
    PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
    /**
     * 预计发货明细
     * @param params 明细id
     */
    PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
    /**
     * 预计采购明细
     * @param params 明细id
     */
    PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);
    /**
     * 店铺库存明细
     *
     * @param params 明细id
     */
    PagingVO<InventoryDetailVO> inventoryDetail(PagingDTO<InventoryTotalDTO> params);
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
     * 批量暂不补货
     *
     * @param ids                   建议id
     * @param replenishmentRemark   备注
     */
    void batchNotRestockingReplenishment(List<String> ids,String replenishmentRemark);
    /**
     * 恢复补货
     * @author will
     * @date 2024/8/29 15:02
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO restoreReplenishment(String id,String replenishmentRemark);
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

    /**
     * 根据唯一键查询（平台、店铺、sku）
     * @author will
     * @date 2024/9/3 16:36
     * @param platformCodeList
     * @param shopIdList
     * @param skuIdList
     * @return List<ReplenishmentSuggestionEntity>
     */
    List<ReplenishmentSuggestionEntity> listByUnique(List<String> platformCodeList, List<String> shopIdList, List<String> skuIdList);

    /**
     * 获取所有需要计算的数据
     * @param platformType 类型
     */
    List<ReplenishmentSuggestionEntity> listCalculationData(String platformType);
    /**
     * 根据id查询标签
     * @author will
     * @date 2024/9/4 16:43
     * @param id
     * @return List<ViewDTO>
     */
    List<LabelInfoDTO.ViewDTO> listLabelInfoById(String id);
    /**
     * 根据id查询标签id集合
     * @author will
     * @date 2024/9/5 11:17
     * @param id
     * @return List<String>
     */
    List<String> listLabelIdById(String id);
    /**
     * 更新备注
     * @author will
     * @date 2024/9/5 11:28
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO updateRemark(String id, String remark);
    /**
     *导出补货规则
     * @author will
     * @date 2024/9/5 19:25
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean exportReplenishmentRule(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);
    /**
     * 导出历史销量
     * @author will
     * @date 2024/9/5 19:36
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean exportHistorySalesQty(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);
    /**
     * 导出补货计划采购建议
     * @author will
     * @date 2024/9/5 19:40
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean exportPurchaseSuggestion(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);
    /**
     * 历史销量导出数据查询
     * @author will
     * @date 2024/9/6 14:54
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    PagingVO<DynamicExcelDTO> listHistorySalesQty(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);

    /**
     * 备货规则导出数据查询
     * @author will
     * @date 2024/9/6 14:54
     * @param dto
     * @return PagingVO<ReplenishmentRuleDTO>
     */
    PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);


    /**
     * 根据店铺id查询销量
     *
     * @param shopIds        店铺id
     * @param skuId          sku
     * @param salesQtyResult 销量规则
     */
    List<LocalInventoryDTO.ShopSalesDTO> getSalesByShopIds(List<String> shopIds, String skuId, CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult);

    /**
     * 保存建议
     * @param cfgRuleStrategy 配置值
     * @param replenishmentResult 建议结果
     */
    void saveReplenishment(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult);

    /**
     * 查询需要计算的数据
     *
     * @param platformType    平台
     * @param salesQtyType    计算类型
     * @param orderType       订单类型
     * @param calculationDate 计算日
     */
    List<ReplenishmentResultDTO> listAllCalculationData(String platformType,String salesQtyType, String orderType, LocalDate calculationDate);

    /**
     * 根据数据类型和订单类型查询历史销量
     *
     * @param replenishmentIds 建议主表id
     * @param salesQtyType     销量数据类型
     * @param orderType        订单类型
     * @param startDate        开始时间
     * @param endDate          结束时间
     */
    List<ReplenishmentResultDTO.SalesHistoryDTO> listSalesHistory(List<String> replenishmentIds, String salesQtyType, String orderType, LocalDate startDate, LocalDate endDate);

    /**
     * 根据数据类型和订单类型查询历史销量
     *
     * @param replenishmentId 建议主表id
     * @param salesQtyType    销量数据类型
     * @param orderType       订单类型
     * @param startDate       开始时间
     * @param endDate         结束时间
     */
    Map<LocalDate, Integer> listSalesHistoryMap(String replenishmentId, String salesQtyType, String orderType, LocalDate startDate, LocalDate endDate);


    /**
     * 库存预测
     * @param dto 参数
     */
    EstimationResultDTO inventoryEstimation(InventoryEstimationDTO dto);

    /**
     * 库存预测明细
     *
     * @param dto 参数
     */
    EstimationDetailResultDTO inventoryEstimationDetail(InventoryEstimationDetailDTO dto);

    /**
     * 库存总数
     * @param params 明细id
     */
    Integer inventoryTotal(InventoryTotalDTO params);

    /**
     * 更新数据
     * @param id 建议主表id
     */
    BatchResultDTO renewData(String id);

    /**
     * 模拟销量分析
     * @param dto 参数
     */
    SalesAnalysisVO mockSalesAnalysis(MockSalesAnalysisDTO dto);
    /**
     * 导出发货建议
     * @author will
     * @date 2024/10/12 14:46
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean exportDeliverySuggest(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);

    /**
     * 根据平台类型查询数据
     * @param platformType 平台类型
     */
    List<ReplenishmentSuggestionEntity> listByPlatform(String platformType);

    /**
     * 获取店铺最近销量
     * @param salesQtyType 销量类型
     * @param orderType   订单类型
     */
    Map<String, Map<String, Integer>> getSalesHistoryMap(String salesQtyType, String orderType);

    /**
     * 导出历史销量
     * @param dto 参数
     */
    PagingVO<CfgRuleCalcDTO.HistorySaleDTO> exportCalcHistorySale(PagingDTO<CfgRuleCalcDTO.DownloadDTO> dto);
}
