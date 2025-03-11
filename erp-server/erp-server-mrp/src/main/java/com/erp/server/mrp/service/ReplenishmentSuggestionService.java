package com.erp.server.mrp.service;

import cn.hutool.json.JSONArray;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.vo.*;

import javax.servlet.http.HttpServletResponse;
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
     *
     * @param params 参数
     */
    PagingVO<ReplenishmentSuggestionVO.PagingView> paging(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params);

    /**
     * 补货建议明细
     *
     * @param detailId 明细id
     */
    ReplenishmentSuggestionVO.View view(String detailId);

    /**
     * fba在途明细明细
     *
     * @param params 明细id
     */
    PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 海外仓在途明细
     *
     * @param params 明细id
     */
    PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 本地仓在途明细
     *
     * @param params 明细id
     */
    PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 预计发货明细
     *
     * @param params 明细id
     */
    PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params);

    /**
     * 预计采购明细
     *
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
     *
     * @param dto 参数
     */
    SalesAnalysisVO salesAnalysis(SalesAnalysisDTO dto);

    /**
     * 历史库存
     *
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
     *
     * @param id
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/29 15:01
     */
    BatchResultDTO notRestockingReplenishment(String id, String replenishmentRemark);


    /**
     * 批量暂不补货
     *
     * @param ids                 建议id
     * @param replenishmentRemark 备注
     */
    void batchNotRestockingReplenishment(List<String> ids, String replenishmentRemark);

    /**
     * 恢复补货
     *
     * @param id
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/29 15:02
     */
    BatchResultDTO restoreReplenishment(String id, String replenishmentRemark);

    /**
     * 批量暂不补货
     *
     * @param ids                 建议id
     * @param replenishmentRemark 备注
     */
    void batchRestockingReplenishment(List<String> ids, String replenishmentRemark);

    /**
     * 批量设置规则
     *
     * @param id
     * @param stockUpUpdateDTO
     * @param salesQtyUpdateDTO
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/29 15:57
     */
    BatchResultDTO batchUpdateRule(String id, CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO, CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO);

    /**
     * 恢复规则设置
     *
     * @param id
     * @param ruleTypeList
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/29 15:57
     */
    BatchResultDTO restoreRule(String id, List<String> ruleTypeList);

    /**
     * 关注补货建议
     *
     * @param id
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/30 10:54
     */
    BatchResultDTO favorite(String id);

    /**
     * 取消关注
     *
     * @param id
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/30 11:09
     */
    BatchResultDTO cancelFavorite(String id);

    /**
     * 添加标签
     *
     * @param updateLabelDTO
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/30 14:51
     */
    BatchResultDTO updateLabel(ReplenishmentSuggestionDTO.UpdateLabelDTO updateLabelDTO);

    /**
     * 批量更新标签
     *
     * @param id
     * @param labelIdList
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/30 16:02
     */
    BatchResultDTO batchAddLabel(String id, List<String> labelIdList);

    /**
     * 取消标签
     *
     * @param id
     * @param labelIdList
     * @return BatchResultDTO
     * @author will
     * @date 2024/8/30 16:15
     */
    BatchResultDTO cancelLabel(String id, List<String> labelIdList);

    /**
     * 获取所有sku和店铺
     */
    List<ReplenishmentSuggestionEntity> listAllSkuAndShop(String type);

    /**
     * 根据唯一键查询（平台、店铺、sku）
     *
     * @param platformCodeList
     * @param shopIdList
     * @param skuIdList
     * @return List<ReplenishmentSuggestionEntity>
     * @author will
     * @date 2024/9/3 16:36
     */
    List<ReplenishmentSuggestionEntity> listByUnique(List<String> platformCodeList, List<String> shopIdList, List<String> skuIdList);

    /**
     * 获取所有需要计算的数据
     *
     * @param platformType 类型
     */
    List<ReplenishmentSuggestionEntity> listCalculationData(String platformType);

    /**
     * 根据id查询标签
     *
     * @param id
     * @return List<ViewDTO>
     * @author will
     * @date 2024/9/4 16:43
     */
    List<LabelInfoDTO.ViewDTO> listLabelInfoById(String id);

    /**
     * 根据id查询标签id集合
     *
     * @param id
     * @return List<String>
     * @author will
     * @date 2024/9/5 11:17
     */
    List<String> listLabelIdById(String id);

    /**
     * 更新备注
     *
     * @param id
     * @param remark
     * @return BatchResultDTO
     * @author will
     * @date 2024/9/5 11:28
     */
    BatchResultDTO updateRemark(String id, String remark);

    /**
     * 导出补货规则
     *
     * @param pagingParamDTO
     * @return Boolean
     * @author will
     * @date 2024/9/5 19:25
     */
    Boolean exportReplenishmentRule(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);

    /**
     * 导出历史销量
     *
     * @param pagingParamDTO
     * @return Boolean
     * @author will
     * @date 2024/9/5 19:36
     */
    Boolean exportHistorySalesQty(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);

    /**
     * 导出补货计划采购建议
     *
     * @param pagingParamDTO
     * @return Boolean
     * @author will
     * @date 2024/9/5 19:40
     */
    Boolean exportPurchaseSuggestion(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);

    /**
     * 历史销量导出数据查询
     *
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     * @author will
     * @date 2024/9/6 14:54
     */
    PagingVO<DynamicExcelDTO> listHistorySalesQty(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);

    /**
     * 备货规则导出数据查询
     *
     * @param dto
     * @return PagingVO<ReplenishmentRuleDTO>
     * @author will
     * @date 2024/9/6 14:54
     */
    PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);

    /**
     * 保存建议
     *
     */
    void saveReplenishment(List<ReplenishmentResultDTO> resultDTOS);

    /**
     * 查询需要计算的数据
     *
     * @param platformType    平台
     * @param salesQtyType    计算类型
     * @param orderType       订单类型
     * @param calculationDate 计算日
     */
    List<ReplenishmentResultDTO> listAllCalculationData(String platformType, String salesQtyType, JSONArray orderType, LocalDate calculationDate);

    /**
     * 根据数据类型和订单类型查询历史销量
     *
     * @param replenishmentIds 建议主表id
     * @param salesQtyType     销量数据类型
     * @param orderType        订单类型
     * @param startDate        开始时间
     * @param endDate          结束时间
     */
    List<ReplenishmentResultDTO.SalesHistoryDTO> listSalesHistory(List<String> replenishmentIds, String salesQtyType, JSONArray orderType, LocalDate startDate, LocalDate endDate);

    /**
     * 根据数据类型和订单类型查询历史销量
     *
     * @param replenishmentId 建议主表id
     * @param salesQtyType    销量数据类型
     * @param orderType       订单类型
     * @param startDate       开始时间
     * @param endDate         结束时间
     */
    Map<LocalDate, Integer> listSalesHistoryMap(String replenishmentId, String salesQtyType, JSONArray orderType, LocalDate startDate, LocalDate endDate);


    /**
     * 库存预测
     *
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
     *
     * @param params 明细id
     */
    Integer inventoryTotal(InventoryTotalDTO params);

    /**
     * 更新数据
     *
     * @param id 建议主表id
     */
    BatchResultDTO renewData(String id);

    /**
     * 模拟销量分析
     *
     * @param dto 参数
     */
    SalesAnalysisVO mockSalesAnalysis(MockSalesAnalysisDTO dto);

    /**
     * 导出发货建议
     *
     * @param pagingParamDTO
     * @return Boolean
     * @author will
     * @date 2024/10/12 14:46
     */
    Boolean exportDeliverySuggest(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO);

    /**
     * 根据平台类型查询数据
     *
     * @param platformType 平台类型
     */
    List<ReplenishmentSuggestionEntity> listByPlatform(String platformType);

    /**
     * 导出历史销量
     */
    List<CfgRuleCalcDTO.HistorySaleDTO> exportCalcHistorySale(CfgRuleCalcDTO.DownloadDTO dto, Object[] searchAfterValues);

    /**
     * 历史销量近365天
     *
     * @param dto 参数
     */
    List<ReplenishmentSuggestionVO.SalesInfoVO> listSalesInfo(BaseIdDTO dto);

    /**
     * 库存总数
     *
     * @param params 明细id
     */
    Integer inventoryDetailTotal(InventoryDetailTotalDTO params);

    /**
     * 导出计算数据
     * @param dto 参数
     */
    void exportCalcData(BaseIdDTO dto);

    /**
     * 导出库存预测依据
     * @param dto 参数
     */
    ReplenishmentSuggestionDTO.ExportResultDTO exportSuggestCalcData(BaseIdDTO dto);

    /**
     * 通过店铺和sku查询建议id
     * @param shopIdList 店铺id
     * @param skuIdList  skuid
     */
    List<ReplenishmentSuggestionEntity> listByShopIdAndSkuId(List<String> shopIdList, List<String> skuIdList);

    /**
     * 临时导出
     * @param exportSalesDTO 导出
     * @param response 响应
     */
    void exportSales(ReplenishmentSuggestionDTO.ExportSalesDTO exportSalesDTO, HttpServletResponse response);
}
