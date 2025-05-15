package com.erp.server.mrp.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.*;
import com.erp.server.mrp.handler.DeliverySuggestionQueryHandler;
import com.erp.server.mrp.handler.OverseasHistoryInventoryHandler;
import com.erp.server.mrp.handler.PurchaseSuggestionMergeQueryHandler;
import com.erp.server.mrp.handler.ReplenishmentSuggestionQueryHandler;
import com.erp.server.mrp.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/feign/export")
public class ExportMrpFeignController {

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private PurchaseSuggestIndependentService purchaseSuggestIndependentService;

    @Resource
    private PurchaseSuggestMergeService purchaseSuggestMergeService;

    @Resource
    private DeliverySuggestService deliverySuggestService;

    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;

    @Resource
    private CalcSalesInfoDimService calcSalesInfoDimService;

    @Resource
    private OverseasHistoryInventoryService overseasHistoryInventoryService;
    @Resource
    private LocalHistoryInventoryService localHistoryInventoryService;
    @Resource
    private VirtualInventoryHistoryService virtualInventoryHistoryService;

    /**
     * 历史销量导出数据查询
     * @author will
     * @date 2024/9/6 14:53
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    @PostMapping("/listHistorySalesQty")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<DynamicExcelDTO> listHistorySalesQty(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return replenishmentSuggestionService.listHistorySalesQty(dto);
    }
    /**
     * (补货建议)采购建议导出数据查询
     * @author will
     * @date 2024/9/6 14:53
     * @param dto
     * @return PagingVO<PurchaseSuggestionDTO>
     */
    @PostMapping("/listPurchaseSuggestion")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return purchaseSuggestIndependentService.listPurchaseSuggestion(dto);
    }

    /**
     * 备货规则导出数据查询
     * @author will
     * @date 2024/9/6 14:53
     * @param dto
     * @return PagingVO<ReplenishmentRuleDTO>
     */
    @PostMapping("/listReplenishmentRule")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return replenishmentSuggestionService.listReplenishmentRule(dto);
    }

    /**
     * (补货建议)发货建议导出数据查询
     * @author will
     * @date 2024/10/12 14:54
     * @param dto
     * @return PagingVO<DeliverySuggestionDTO>
     */
    @PostMapping("/listDeliverySuggestion")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> listDeliverySuggestion(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return deliverySuggestService.listDeliverySuggestion(dto);
    }

    /**
     * 采购建议导出数据查询
     * @author will
     * @date 2024/10/17 10:41
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/pagingPurchaseSuggestion")
    @WebAdvanceQuery(handler = PurchaseSuggestionMergeQueryHandler.class)
    public PagingVO<PurchaseSuggestIndependentDTO.ListDTO> pagingPurchaseSuggestion(@RequestBody PagingDTO<PurchaseSuggestIndependentDTO.PagingParamDTO> dto) {
        return purchaseSuggestIndependentService.paging(dto);
    }

    /**
     * 采购建议(合并)导出数据查询
     * @author will
     * @date 2024/10/17 10:41
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/pagingPurchaseSuggestionMerge")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "psm.shop_id",
            menuCode = "mrp:purchaseSuggestMerge:paging"
    )
    @WebAdvanceQuery(handler = PurchaseSuggestionMergeQueryHandler.class)
    public PagingVO<PurchaseSuggestMergeDTO.ListDTO> pagingPurchaseSuggestionMerge(@RequestBody PagingDTO<PurchaseSuggestMergeDTO.PagingParamDTO> dto) {
        return purchaseSuggestMergeService.paging(dto);
    }

    /**
     * 发货建议导出数据查询
     * @author will
     * @date 2024/10/17 10:44
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/pagingDeliverySuggestion")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "ds.shop_id",
            menuCode = "mrp:deliverySuggest:paging"
    )
    @WebAdvanceQuery(handler = DeliverySuggestionQueryHandler.class)
    public PagingVO<DeliverySuggestDTO.ListDTO> pagingDeliverySuggestion(@RequestBody PagingDTO<DeliverySuggestDTO.PagingParamDTO> dto) {
        return deliverySuggestService.paging(dto);
    }


    /**
     * fba库存
     * @param dto 参数
     */
    @PostMapping("/fbaInventory")
    @WebAdvanceQuery
    public PagingVO<FbaHistoryInventoryDTO.ListDTO> exportFbaInventory(@RequestBody PagingDTO<FbaHistoryInventoryDTO.ExportDTO> dto) {
        return fbaHistoryInventoryService.exportFbaInventory(dto);
    }

    /**
     * 导出历史销量
     * @param dto 参数
     */
    @PostMapping("/exportCalcHistorySale")
    public List<CfgRuleCalcDTO.HistorySaleDTO> exportCalcHistorySale(@RequestBody CfgRuleCalcDTO.DownloadDTO dto) {
        return replenishmentSuggestionService.exportCalcHistorySale(dto, dto.getSearchAfterValues());
    }

    /**
     * 海外仓每日库存
     */
    @PostMapping("/overseasInventory")
    @WebAdvanceQuery(handler = OverseasHistoryInventoryHandler.class)
    public PagingVO<OverseasHistoryInventoryDTO.ListDTO> exportOverseasInventory(@RequestBody PagingDTO<OverseasHistoryInventoryDTO.ExportDTO> dto){
        return overseasHistoryInventoryService.exportOverseasInventory(dto);
    }

    /**
     * 本地仓每日库存
     */
    @PostMapping("/localInventory")
    @WebAdvanceQuery
    public PagingVO<LocalHistoryInventoryDTO.PagingViewDTO> exportLocalInventory(@RequestBody PagingDTO<LocalHistoryInventoryDTO.ExportDTO> dto){
        return localHistoryInventoryService.exportLocalInventory(dto);
    }
    /**
     * 导出试算逻辑
     * @param dto 参数
     */
    @PostMapping("/getListExportData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "csic.shop_id",
            menuCode = "mrp:calcSalesInfoDim:paging"
    )
    @WebAdvanceQuery
    public PagingVO<CalcSalesInfoDimDTO.ExportResultDTO> getListExportData(@RequestBody PagingDTO<CalcSalesInfoDimDTO.ExportSalesInfoDTO> dto){
        return calcSalesInfoDimService.getListExportData(dto);
    }

    /**
     * 导出虚拟仓库存
     * @param dto 参数
     */
    @PostMapping("/getVirtualInventory")
    @WebAdvanceQuery
    public PagingVO<VirtualInventoryHistoryDTO.ListDTO> getVirtualInventory(@RequestBody PagingDTO<VirtualInventoryHistoryDTO.SearchParamDTO> dto){
        return virtualInventoryHistoryService.getVirtualInventory(dto);
    }

    /**
     * 导出试算列表
     * @param dto 参数
     */
    @PostMapping("/exportMrpSalesCalcList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "csic.shop_id",
            menuCode = "mrp:calcSalesInfoDim:pagingDetail"
    )
    @WebAdvanceQuery
    public PagingVO<CalcSalesInfoDimDTO.ExportSalesInfoListDTO> exportMrpSalesCalcList(@RequestBody PagingDTO<CalcSalesInfoDimDTO.ParamDTO> dto) {
        return calcSalesInfoDimService.exportMrpSalesCalcList(dto);
    }

    /**
     * 导出试算列表
     * @param dto 参数
     */
    @PostMapping("/exportMrpSalesCalcTemplateList")
    @WebAdvanceQuery
    public PagingVO<CalcSalesInfoDimDTO.ExportSalesInfoTemplateListDTO> exportMrpSalesCalcTemplateList(@RequestBody PagingDTO<CalcSalesInfoDimDTO.ParamDTO> dto){
        return calcSalesInfoDimService.exportMrpSalesCalcTemplateList(dto);
    }


    /**
     * 导出库存预测依据
     * @param dto 参数
     */
    @PostMapping("/exportSuggestCalcData")
    public ReplenishmentSuggestionDTO.ExportResultDTO exportSuggestCalcData(@RequestBody BaseIdDTO dto) {
        return replenishmentSuggestionService.exportSuggestCalcData(dto);
    }

}
