package com.erp.rpc.wms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.ExportQcReportExcelDTO;
import com.erp.model.wms.dto.excel.QcBillExportExcelDTO;
import com.erp.model.wms.dto.excel.WarehouseExportExcelDTO;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.vo.WarehouseLocationExportVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-wms", contextId = "exportWmsFeign", configuration = ExportFeignConfig.class)
public interface ExportWmsFeign {

    @PostMapping("/feign/export/b2cDelivery")
    PagingVO<SoB2cDeliveryDTO.ListDTO> exportB2cDelivery(@RequestBody PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/packageForecast")
    PagingVO<PackageForecastDTO.ExportViewDTO> exportPackageForecast(@RequestBody PagingDTO<PackageForecastDTO.ExportDTO> dto);

    @PostMapping("/feign/export/warehouseLocation")
    PagingVO<WarehouseLocationExportVo> exportWarehouseLocation(@RequestBody PagingDTO<WarehouseLocationDTO.exportParamDto> dto);

    @PostMapping("/feign/export/requisitionApplication")
    PagingVO<RequisitionApplicationDTO.ListDTO> exportRequisitionApplication(@RequestBody PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/requisitionApplicationChange")
    PagingVO<RequisitionApplicationChangeDTO.ListDTO> exportRequisitionApplicationChange(@RequestBody PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/warehouseLocationSafetyInventory")
    PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> exportWarehouseLocationSafetyInventory(@RequestBody PagingDTO<WarehouseLocationSafetyInventoryDTO.exportParamDTO> dto);

    @PostMapping("/feign/export/listDiffExportData")
    PagingVO<VirtualInventoryDiffDTO.ListDiffExportDataDTO> exportListDiffExportData(@RequestBody PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto);

    /**
     * 暂时不用
     */
    @PostMapping("/feign/export/warehouseStatisticsData")
    PagingVO<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> exportWarehouseStatisticsData(@RequestBody PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/virtualWarehouseAllocation")
    PagingVO<VirtualWarehouseAllocationDTO.ListDTO> exportVirtualWarehouseAllocation(@RequestBody PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto);

    @PostMapping("/feign/export/aliexpressDelivery")
    PagingVO<AliexpressDeliveryDTO.ListDTO> exportAliexpressDelivery(@RequestBody PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/fbaDelivery")
    PagingVO<FirstMileDeliveryDTO.ListDTO> exportFbaDelivery(@RequestBody PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/getInventoryPageData")
    PagingVO<InventoryDTO.PagingViewDTO> getInventoryPageData(@RequestBody PagingDTO<InventoryDTO.ExportSearchParamDTO> dto);

    @PostMapping("/feign/export/dailyQcBill")
    PagingVO<QcInfoDTO.QcDailyReportDTO> exportDailyQcBill(@RequestBody PagingDTO<QcInfoDTO.ExportDTO> dto);

    @PostMapping("/feign/export/fbaInventory")
    PagingVO<FbaInventoryDTO.ListDTO> exportFbaInventory(@RequestBody PagingDTO<FbaInventoryDTO.ExportDTO> dto);

    @PostMapping("/feign/export/fbaShipment")
    PagingVO<FbaShipmentDTO.ExportDTO> exportFbaShipment(@RequestBody PagingDTO<FbaShipmentDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/initStock")
    PagingVO<InitStockDTO.ListDTO> exportInitStock(@RequestBody PagingDTO<InitStockDTO.ExportSearchParamDTO> dto);

    @PostMapping("/feign/export/inventoryDaily")
    PagingVO<InventoryReportDTO.ListDailyInventoryDTO> exportInventoryDaily(@RequestBody PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto);

    @PostMapping("/feign/export/inventoryInOutStock")
    PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> exportInventoryInOutStock(@RequestBody PagingDTO<InventoryDTO.ExportInOutStockTransFlowSearchParamDTO> dto);

    @PostMapping("/feign/export/inOutStockSummary")
    PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> exportInOutStockSummary(@RequestBody PagingDTO<InventoryDTO.ExcelInOutStockSummarySearchParamDTO> dto);

    @PostMapping("/feign/export/inventoryTransFlow")
    PagingVO<InventoryDTO.TransFlowPagingViewDTO> exportInventoryTransFlow(@RequestBody PagingDTO<InventoryDTO.ExportInvFlowSearchParamDTO> dto);

    @PostMapping("/feign/export/inventoryTransport")
    PagingVO<InventoryReportDTO.TransportPagingDTO> exportInventoryTransport(@RequestBody PagingDTO<InventoryReportDTO.ExportTransportSearchParamDTO> dto);

    @PostMapping("/feign/export/machineInfo")
    PagingVO<MachineInfoDTO.ListDTO> exportMachineInfo(@RequestBody PagingDTO<MachineInfoDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/otherInStock")
    PagingVO<OtherInstockDTO.ListDTO> exportOtherInStock(@RequestBody PagingDTO<OtherInstockDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/otherOutStock")
    PagingVO<OtherOutstockDTO.ListDTO> exportOtherOutStock(@RequestBody PagingDTO<OtherOutstockDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/overseasDeliveryPlan")
    PagingVO<WmsDeliveryPlanDTO.ListDTO> exportOverseasDeliveryPlan(@RequestBody PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/overseasInventory")
    PagingVO<OverseasInventoryDTO.ListDTO> exportOverseasInventory(@RequestBody PagingDTO<OverseasInventoryDTO.ExportDTO> dto);

    @PostMapping("/feign/export/overseasWarehouseInbound")
    PagingVO<OverseasWarehouseInboundDTO.ListDTO> exportOverseasWarehouseInbound(@RequestBody PagingDTO<OverseasWarehouseInboundDTO.ExportDTO> dto);

    @PostMapping("/feign/export/packingTaskDetail")
    PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto);
    @PostMapping("/feign/export/exportPackingTaskDetailMerge")
    PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetailMerge(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto);
    @PostMapping("/feign/export/unPackingTaskDetail")
    PagingVO<WmsCartonSpecDTO.NoPackingViewDTO> unPackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/firstMilePackingTaskDetail")
    PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> firstMilePackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/packingTask")
    PagingVO<PackingTaskDTO.PagingViewDTO> exportPackingTask(@RequestBody PagingDTO<PackingTaskDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/pickingLists")
    PagingVO<PickingListsDTO.ExportInfoDTO> exportPickingLists(@RequestBody PagingDTO<PickingListsDTO.ExportDTO> dto);

    @PostMapping("/feign/export/poInStock")
    PagingVO<PoInstockDTO.ListDTO> exportPoInStock(@RequestBody PagingDTO<PoInstockDTO.ExportParamDTO> dto);

    @PostMapping("/feign/export/purchaseBusiness")
    PagingVO<PurchaseBusinessGatherTableDTO.PagingViewDTO> exportPurchaseBusiness(@RequestBody PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/purchaseReturnOrder")
    PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> exportPurchaseReturnOrder(@RequestBody PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/qcBill")
    PagingVO<QcBillExportExcelDTO> exportQcBill(@RequestBody PagingDTO<QcInfoDTO.ExportDTO> dto);

    @PostMapping("/feign/export/qcEffectivenessDocument")
    PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> exportQcEffectivenessDocument(@RequestBody PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto);

    @PostMapping("/feign/export/qcEffectivenessPersonnel")
    PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> exportQcEffectivenessPersonnel(@RequestBody PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto);

    @PostMapping("/feign/export/qcReportDetail")
    PagingVO<ExportQcReportExcelDTO> exportQcReportDetail(@RequestBody PagingDTO<BaseIdDTO> dto);

    @PostMapping("/feign/export/soDeliveryNotice")
    PagingVO<SoDeliveryNoticeDTO.PagingView> exportSoDeliveryNotice(@RequestBody PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto);

    @PostMapping("/feign/export/soDeliveryNoticeChange")
    PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> exportSoDeliveryNoticeChange(@RequestBody PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/soOutStock")
    PagingVO<SoOutstockDTO.PagingViewDTO> exportSoOutStock(@RequestBody PagingDTO<SoOutstockDTO.ExportDTO> dto);

    @PostMapping("/feign/export/soReturnInStock")
    PagingVO<SoReturnInstockDTO.PagingView> exportSoReturnInStock(@RequestBody PagingDTO<SoReturnInstockDTO.PagingParam> dto);

    @PostMapping("/feign/export/soReturnNotice")
    PagingVO<SoReturnNoticeDTO.PagingView> exportSoReturnNotice(@RequestBody PagingDTO<SoReturnNoticeDTO.PagingParam> dto);

    @PostMapping("/feign/export/soReturnReceive")
    PagingVO<SoReturnReceiveDTO.PagingView> exportSoReturnReceive(@RequestBody PagingDTO<SoReturnReceiveDTO.PagingParam> dto);

    @PostMapping("/feign/export/stocktakingProfitLoss")
    PagingVO<StocktakingProfitLossDTO.ExportViewDTO> exportStocktakingProfitLoss(@RequestBody PagingDTO<StocktakingProfitLossDTO.ExportDTO> dto);

    @PostMapping("/feign/export/stocktakingTaskDetail")
    PagingVO<StocktakingTaskDetailDTO.ExportDTO> exportStocktakingTaskDetail(@RequestBody PagingDTO<StocktakingTaskDTO.BaseIdDTO> dto);

    @PostMapping("/feign/export/subcontractIssue")
    PagingVO<SubcontractIssueDTO.ListDTO> exportSubcontractIssue(@RequestBody PagingDTO<SubcontractIssueDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/subcontractReturn")
    PagingVO<SubcontractReturnDTO.ListDTO> exportSubcontractReturn(@RequestBody PagingDTO<SubcontractReturnDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/supplierDeliveryOrder")
    PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(@RequestBody PagingDTO<DeliveryOrderDTO.ParamDTO> dto);

    @PostMapping("/feign/export/transferApplication")
    PagingVO<TransferApplicationDTO.ListDTO> exportTransferApplication(@RequestBody PagingDTO<TransferApplicationDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/transferInfo")
    PagingVO<TransferInfoDTO.ListDTO> exportTransferInfo(@RequestBody PagingDTO<TransferInfoDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/transferIn")
    PagingVO<TransferInDTO.PagingViewDTO> exportTransferIn(@RequestBody PagingDTO<TransferInDTO.ExportDTO> dto);

    @PostMapping("/feign/export/transferOut")
    PagingVO<TransferOutDTO.PagingViewDTO> exportTransferOut(@RequestBody PagingDTO<TransferOutDTO.ExportDTO> dto);

    @PostMapping("/feign/export/virtualTransFlow")
    PagingVO<VirtualTransFlowDTO.ListDTO> exportVirtualTransFlow(@RequestBody PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/warehouse")
    PagingVO<WarehouseExportExcelDTO> exportWarehouse(@RequestBody PagingDTO<WarehouseDTO.ExportDTO> dto);

    @PostMapping("/feign/export/warehouseLocationMoveInfo")
    PagingVO<WarehouseLocationMoveDTO.PdaPcListDTO> exportWarehouseLocationMoveInfo(@RequestBody PagingDTO<WarehouseLocationMoveDTO.ExportDTO> dto);

    @PostMapping("/feign/export/warehouseLocationReplenish")
    PagingVO<WarehouseLocationReplenishDTO.ViewDTO> exportWarehouseLocationReplenish(@RequestBody PagingDTO<WarehouseLocationReplenishDTO.ExportParamDTO> dto);

    @PostMapping("/feign/export/warehouseReceive")
    PagingVO<WarehouseReceiveExportExcelDTO> exportWarehouseReceive(@RequestBody PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/virtualInventory")
    PagingVO<VirtualInventoryDTO.ListDTO> getVirtualInventory(@RequestBody PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/inventoryAge")
    PagingVO<DynamicExcelDTO> exportWmsInventoryAge(@RequestBody PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto);

    @PostMapping("/feign/export/fbaShipmentPacking")
    PagingVO<FbaShipmentPackingDTO.ViewDTO> exportFbaShipmentPacking(@RequestBody PagingDTO<FbaShipmentDTO.PagingParamDTO> dto);

    /**
     * 订单需求明细导出
     */
    @PostMapping("/feign/export/listReportOrderDemandDetail")
    PagingVO<ReportOrderDemandDetailDTO.ListDTO> listReportOrderDemandDetail(@RequestBody PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> dto);
    /**
     * 缺货统计导出
     */
    @PostMapping("/feign/export/listReportOrderDemand")
    PagingVO<ReportOrderDemandDTO.ListDTO> listReportOrderDemand(@RequestBody PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto);
    /**
     * 销售看板导出
     */
    @PostMapping("/feign/export/listReportOrderSales")
    PagingVO<ReportOrderSalesDTO.ListDTO> listReportOrderSales(@RequestBody PagingDTO<ReportOrderSalesDTO.PagingParamDTO> dto);

    /**
     * 偏远邮编导出
     */
    @PostMapping("/feign/export/exportRemotePostcode")
    PagingVO<RemotePostcodeDTO.ExportListDTO> exportRemotePostcode(@RequestBody PagingDTO<RemotePostcodeDTO.PagingParamDTO> dto);

    /**
     * 导出虚拟仓分货
     */
    @PostMapping("/feign/export/exportVirtualStatistics")
    PagingVO<VirtualWarehouseAllocationDTO.ExportStatisticsDTO> exportVirtualStatistics(PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto);
    /**
     * 库龄分析导出
     */
    @PostMapping("/feign/export/exportWmsVirtualInventoryAge")
    PagingVO<DynamicExcelDTO> exportWmsVirtualInventoryAge(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto);
    /**
     * 历史库龄导出
     */
    @PostMapping("/feign/export/hisInventoryAgePaging")
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto);

    /**
     * 历史库龄明细导出
     */
    @PostMapping("/feign/export/hisInventoryAgeDetailPaging")
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto);

    /**
     * 库龄流水导出
     */
    @PostMapping("/feign/export/virtualTransFlowDetailPaging")
    PagingVO<VirtualTransFlowDetailDTO.ListDTO> virtualTransFlowDetailPaging(PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto);
    /**
     * 列表历史库龄明细导出
     */
    @PostMapping("/feign/export/framePaging")
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> framePaging(PagingDTO<VirtualInventoryAgeDTO.FrameParamDTO> dto);
    /**
     * 导出b2b销售订单虚拟仓订单跟踪
     */
    @PostMapping("/feign/export/exportSoB2bProcessing")
    PagingVO<SoB2bProcessingDTO.ListDTO> exportSoB2bProcessing(PagingDTO<SoB2bProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出b2c销售订单虚拟仓订单跟踪
     */
    @PostMapping("/feign/export/exportSoB2cProcessing")
    PagingVO<SoB2cProcessingDTO.ListDTO> exportSoB2cProcessing(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出头程销售订单虚拟仓订单跟踪
     */
    @PostMapping("/feign/export/exportFirstMileProcessing")
    PagingVO<FirstMileProcessingDTO.ListDTO> exportFirstMileProcessing(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出FBA在途核对列表
     */
    @PostMapping("/feign/export/exportFbaTransitReport")
    PagingVO<FbaTransitCalculateReportDTO.ListDTO> exportFbaTransitReport(PagingDTO<FbaTransitCalculateReportDTO.PagingParamDTO> dto);

    /**
     * 导出虚拟仓设置
     */
    @PostMapping("/feign/export/exportVirtualWarehouse")
    PagingVO<VirtualWarehouseDTO.ExportDTO> exportVirtualWarehouse(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto);
    /**
     * 导出质检通知单
     */
    @PostMapping("/feign/export/exportQcNotice")
    PagingVO<QcNoticeDTO.ListDTO> exportQcNotice(@RequestBody PagingDTO<QcNoticeDTO.ExportDTO> dto);
    /**
     * 导出虚拟库存调整
     */
    @PostMapping("/feign/export/exportVirtualAdjust")
    PagingVO<VirtualAdjustDTO.ListDTO> exportVirtualAdjust(@RequestBody PagingDTO<VirtualAdjustDTO.PagingParamDTO> dto);

    /**
     * 导出即时库存
     */
    @PostMapping("/feign/export/supplierInventory")
    PagingVO<SupplierInventoryDTO.ListDTO> exportSupplierInventory(PagingDTO<SupplierInventoryDTO.PagingParamDTO> dto);

    /**
     * 导出三方仓发货单
     */
    @PostMapping("/feign/export/exportThirdWarehouseDelivery")
    PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> exportThirdWarehouseDelivery(@RequestBody PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto);

    /**
     * 导出样品报废单
     */
    @PostMapping("/feign/export/exportSampleScrapInfo")
    PagingVO<SampleScrapInfoDTO.ListDTO> exportSampleScrapInfo(@RequestBody PagingDTO<SampleScrapInfoDTO.PagingParamDTO> dto);

    /**
     * 导出样品领用单
     */
    @PostMapping("/feign/export/getSampleRecipientPageData")
    PagingVO<SampleRecipientDTO.ListDTO> getSampleRecipientPageData(@RequestBody PagingDTO<SampleRecipientDTO.ExportDTO> dto);

    /**
     * 导出样品台账统计
     */
    @PostMapping("/feign/export/exportSampleLedger")
    PagingVO<SampleLedgerDTO.ListDTO> exportSampleLedger(@RequestBody PagingDTO<SampleLedgerDTO.ExportDTO> dto);

    /**
     * 导出样品台账流水
     */
    @PostMapping("/feign/export/exportSampleLedgerFlow")
    PagingVO<SampleLedgerFlowDTO.ListDTO> exportSampleLedgerFlow(@RequestBody PagingDTO<SampleLedgerFlowDTO.ExportDTO> dto);
}
