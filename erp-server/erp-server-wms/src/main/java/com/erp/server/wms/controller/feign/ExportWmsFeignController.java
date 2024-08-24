package com.erp.server.wms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
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
import com.erp.server.wms.query.*;
import com.erp.server.wms.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportWmsFeignController {
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private PackageForecastService packageForecastService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    private WarehouseLocationSafetyInventoryService warehouseLocationSafetyInventoryService;
    @Resource
    private VirtualInventoryDiffService virtualInventoryDiffService;
    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;
    @Resource
    private AliexpressDeliveryService aliexpressDeliveryService;

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private QcInfoService qcInfoService;
    @Resource
    private FbaInventoryService fbaInventoryService;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private InitStockService initStockService;
    @Resource
    private TransactionFlowService transactionFlowService;
    @Resource
    private MachineInfoService machineInfoService;
    @Resource
    private OtherInstockService otherInstockService;
    @Resource
    private OtherOutstockService otherOutstockService;
    @Resource
    private WmsDeliveryPlanService wmsDeliveryPlanService;
    @Resource
    private OverseasInventoryService overseasInventoryService;
    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private PackingTaskService packingTaskService;
    @Resource
    private PickingListsService pickingListsService;
    @Resource
    private PoInstockService poInstockService;
    @Resource
    private ReportFormsManageService reportFormsManageService;
    @Resource
    private PoReturnService poReturnService;
    @Resource
    private QcEffectivenessService qcEffectivenessService;
    @Resource
    private QcReportDetailService qcReportDetailService;
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private SoReturnInstockService soReturnInstockService;
    @Resource
    private SoReturnNoticeService soReturnNoticeService;
    @Resource
    private SoReturnReceiveService soReturnReceiveService;
    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;
    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;
    @Resource
    private SubcontractIssueService subcontractIssueService;
    @Resource
    private TransferApplicationService transferApplicationService;
    @Resource
    private SupplierDeliveryOrderService supplierDeliveryOrderService;
    @Resource
    private TransferInfoService transferInfoService;
    @Resource
    private WarehouseReceiveService warehouseReceiveService;
    @Resource
    private WarehouseLocationReplenishService warehouseLocationReplenishService;
    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private VirtualTransFlowService virtualTransFlowService;
    @Resource
    private TransferOutService transferOutService;
    @Resource
    private TransferInService transferInService;
    @Resource
    private VirtualInventoryService virtualInventoryService;
    @PostMapping("/b2cDelivery")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:paging",
            tableAlias = "sbd"
    )
    @WebAdvanceQuery(handler = SoB2cDeliveryQueryHandler.class)
    public PagingVO<SoB2cDeliveryDTO.ListDTO> exportB2cDelivery(@RequestBody PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return soB2cDeliveryService.exportB2cDelivery(dto);
    }

    @PostMapping("/packageForecast")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packageForecast:paging",
            tableAlias = "pf"
    )
    @WebAdvanceQuery(handler = PackageForecastQueryHandler.class)
    public PagingVO<PackageForecastDTO.ExportViewDTO> exportPackageForecast(@RequestBody PagingDTO<PackageForecastDTO.ExportDTO> dto) {
        return packageForecastService.exportPackageForecast(dto);
    }

    @PostMapping("/warehouseLocation")
    @WebAdvanceQuery(handler = WarehouseLocationInfoQueryHandler.class)
    public PagingVO<WarehouseLocationExportVo> exportWarehouseLocation(@RequestBody PagingDTO<WarehouseLocationDTO.exportParamDto> dto) {
        return warehouseLocationService.exportWarehouseLocation(dto);
    }

    @PostMapping("/requisitionApplication")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:exportExcel",
            tableAlias = "ra"
    )
    @WebAdvanceQuery(handler = RequisitionApplicationQueryHandler.class)
    public PagingVO<RequisitionApplicationDTO.ListDTO> exportRequisitionApplication(@RequestBody PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto) {
        return requisitionApplicationService.exportRequisitionApplication(dto);
    }

    @PostMapping("/warehouseLocationSafetyInventory")
    public PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> exportWarehouseLocationSafetyInventory(@RequestBody PagingDTO<WarehouseLocationSafetyInventoryDTO.exportParamDTO> dto) {
        return warehouseLocationSafetyInventoryService.exportWarehouseLocationSafetyInventory(dto);
    }

    @PostMapping("/listDiffExportData")
    @WebAdvanceQuery(handler = VirtualInventoryDiffQueryHandler.class)
    public PagingVO<VirtualInventoryDiffDTO.ListDiffExportDataDTO> exportListDiffExportData(@RequestBody PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return virtualInventoryDiffService.exportListDiffExportData(dto);
    }

    @PostMapping("/warehouseStatisticsData")
    @WebAdvanceQuery(handler = VirtualInventoryDiffQueryHandler.class)
    public PagingVO<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> exportWarehouseStatisticsData(@RequestBody PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return virtualInventoryDiffService.exportWarehouseStatisticsData(dto);
    }

    @PostMapping("/virtualWarehouseAllocation")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:export",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "id")
    @WebAdvanceQuery(handler = VirtualWarehouseAllocationQueryHandler.class)
    public PagingVO<VirtualWarehouseAllocationDTO.ListDTO> exportVirtualWarehouseAllocation(@RequestBody PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto) {
        return virtualWarehouseAllocationService.exportVirtualWarehouseAllocation(dto);
    }

    @PostMapping("/aliexpressDelivery")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:aliexpressDelivery:paging",
            tableAlias = "ad"
    )
    @WebAdvanceQuery(handler = AliexpressDeliveryQueryHandler.class)
    public PagingVO<AliexpressDeliveryDTO.ListDTO> exportAliexpressDelivery(@RequestBody PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        return aliexpressDeliveryService.exportAliexpressDelivery(dto);
    }

    @PostMapping("/fbaDelivery")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:export",
            tableAlias = "fd"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<FirstMileDeliveryDTO.ListDTO> exportFbaDelivery(@RequestBody PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        return firstMileDeliveryService.exportFbaDelivery(dto);
    }

    @PostMapping("/getInventoryPageData")
    PagingVO<InventoryDTO.PagingViewDTO> getInventoryPageData(@RequestBody PagingDTO<InventoryDTO.ExportSearchParamDTO> dto) {
        return inventoryService.getInventoryPageData(dto);
    }

    @PostMapping("/dailyQcBill")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:exportQcBill",
            tableAlias = "qb")
    @WebAdvanceQuery(handler = QcInfoQueryHandler.class)
    public PagingVO<QcInfoDTO.QcDailyReportDTO> exportDailyQcBill(@RequestBody PagingDTO<QcInfoDTO.ExportDTO> dto) {
        return qcInfoService.exportDailyQcBill(dto);
    }

    @PostMapping("/fbaInventory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaInventory:export",
            tableAlias = "fi"
    )
    public PagingVO<FbaInventoryDTO.ListDTO> exportFbaInventory(@RequestBody PagingDTO<FbaInventoryDTO.ExportDTO> dto) {
        return fbaInventoryService.exportFbaInventory(dto);
    }

    @PostMapping("/fbaShipment")
    @WebAdvanceQuery
    public PagingVO<FbaShipmentDTO.ExportDTO> exportFbaShipment(@RequestBody PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        return fbaShipmentService.exportFbaShipment(dto);
    }

    @PostMapping("/initStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:initStock:paging",
            tableAlias = "ism"
    )
    @WebAdvanceQuery
    public PagingVO<InitStockDTO.ListDTO> exportInitStock(@RequestBody PagingDTO<InitStockDTO.ExportSearchParamDTO> dto) {
        return initStockService.exportInitStock(dto);
    }

    @PostMapping("/inventoryAge")
    public PagingVO<InventoryDTO.PagingViewDTO> exportInventoryAge(@RequestBody PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto) {
        return inventoryService.exportInventoryAge(dto);
    }

    @PostMapping("/inventoryDaily")
    public PagingVO<InventoryReportDTO.ListDailyInventoryDTO> exportInventoryDaily(@RequestBody PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto) {
        return transactionFlowService.exportInventoryDaily(dto);
    }

    @PostMapping("/inventoryInOutStock")
    public PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> exportInventoryInOutStock(@RequestBody PagingDTO<InventoryDTO.ExportInOutStockTransFlowSearchParamDTO> dto) {
        return transactionFlowService.exportInventoryInOutStock(dto);
    }

    @PostMapping("/inOutStockSummary")
    public PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> exportInOutStockSummary(@RequestBody PagingDTO<InventoryDTO.ExcelInOutStockSummarySearchParamDTO> dto) {
        return transactionFlowService.exportInOutStockSummary(dto);
    }

    @PostMapping("/inventoryTransFlow")
    public PagingVO<InventoryDTO.TransFlowPagingViewDTO> exportInventoryTransFlow(@RequestBody PagingDTO<InventoryDTO.ExportInvFlowSearchParamDTO> dto) {
        return transactionFlowService.exportInventoryTransFlow(dto);
    }

    @PostMapping("/inventoryTransport")
    public PagingVO<InventoryReportDTO.TransportPagingDTO> exportInventoryTransport(@RequestBody PagingDTO<InventoryReportDTO.ExportTransportSearchParamDTO> dto) {
        return transactionFlowService.exportInventoryTransport(dto);
    }

    @PostMapping("/machineInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:paging",
            tableAlias = "mi"
    )
    @WebAdvanceQuery(handler = MachineInfoQueryHandler.class)
    public PagingVO<MachineInfoDTO.ListDTO> exportMachineInfo(@RequestBody PagingDTO<MachineInfoDTO.SearchParamDTO> dto) {
        return machineInfoService.exportMachineInfo(dto);
    }

    @PostMapping("/otherInStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:paging",
            tableAlias = "oi"
    )
    @WebAdvanceQuery(handler = OtherInstockQueryHandler.class)
    public PagingVO<OtherInstockDTO.ListDTO> exportOtherInStock(@RequestBody PagingDTO<OtherInstockDTO.SearchParamDTO> dto) {
        return otherInstockService.exportOtherInStock(dto);
    }

    @PostMapping("/otherOutStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherOutstock:paging",
            tableAlias = "oo"
    )
    @WebAdvanceQuery(handler = OtherOutstockQueryHandler.class)
    public PagingVO<OtherOutstockDTO.ListDTO> exportOtherOutStock(@RequestBody PagingDTO<OtherOutstockDTO.SearchParamDTO> dto) {
        return otherOutstockService.exportOtherOutStock(dto);
    }

    @PostMapping("/overseasDeliveryPlan")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:export",
            tableAlias = "odp"
    )
    @WebAdvanceQuery(handler = WmsDeliveryPlanQueryHandler.class)
    public PagingVO<WmsDeliveryPlanDTO.ListDTO> exportOverseasDeliveryPlan(@RequestBody PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> dto) {
        return wmsDeliveryPlanService.exportOverseasDeliveryPlan(dto);
    }

    @PostMapping("/overseasInventory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "vms:overseasInventory:exportExcel",
            tableAlias = "op")
    public PagingVO<OverseasInventoryDTO.ListDTO> exportOverseasInventory(@RequestBody PagingDTO<OverseasInventoryDTO.ExportDTO> dto) {
        return overseasInventoryService.exportOverseasInventory(dto);
    }

    @PostMapping("/overseasWarehouseInbound")
    @WebAdvanceQuery
    public PagingVO<OverseasWarehouseInboundDTO.ListDTO> exportOverseasWarehouseInbound(@RequestBody PagingDTO<OverseasWarehouseInboundDTO.ExportDTO> dto) {
        return overseasWarehouseInboundService.exportOverseasWarehouseInbound(dto);
    }

    @PostMapping("/packingTaskDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:exportPackingDetail",
            tableAlias = "pt"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        return packingTaskService.exportPackingTaskDetail(dto);
    }

    @PostMapping("/packingTask")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:exportPacking",
            tableAlias = "pt"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<PackingTaskDTO.PagingViewDTO> exportPackingTask(@RequestBody PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        return packingTaskService.exportPackingTask(dto);
    }

    @PostMapping("/pickingLists")
    @WebAdvanceQuery
    public PagingVO<PickingListsDTO.ExportInfoDTO> exportPickingLists(@RequestBody PagingDTO<PickingListsDTO.ExportDTO> dto) {
        return pickingListsService.exportPickingLists(dto);
    }

    @PostMapping("/poInStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    @WebAdvanceQuery(handler = PoInStockQueryHandler.class)
    public PagingVO<PoInstockDTO.ListDTO> exportPoInStock(@RequestBody PagingDTO<PoInstockDTO.ExportParamDTO> dto) {
        return poInstockService.exportPoInStock(dto);
    }

    @PostMapping("/purchaseBusiness")
    public PagingVO<PurchaseBusinessGatherTableDTO.PagingViewDTO> exportPurchaseBusiness(@RequestBody PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> dto) {
        return reportFormsManageService.exportPurchaseBusiness(dto);
    }

    @PostMapping("/purchaseReturnOrder")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "return_user_id",
            menuCode = "wms:purchaseReturnOrder:paging",
            tableAlias = "pro"
    )
    @WebAdvanceQuery(handler = PoReturnQueryHandler.class)
    public PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> exportPurchaseReturnOrder(@RequestBody PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto) {
        return poReturnService.exportPurchaseReturnOrder(dto);
    }

    @PostMapping("/qcBill")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:exportQcBill",
            tableAlias = "qb")
    @WebAdvanceQuery(handler = QcInfoQueryHandler.class)
    public PagingVO<QcBillExportExcelDTO> exportQcBill(@RequestBody PagingDTO<QcInfoDTO.ExportDTO> dto) {
        return qcInfoService.exportQcBill(dto);
    }

    @PostMapping("/qcEffectivenessDocument")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcEffectiveness:viewQcOverview",
            tableAlias = "qb"
    )
    public PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> exportQcEffectivenessDocument(@RequestBody PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto) {
        return qcEffectivenessService.exportQcEffectivenessDocument(dto);
    }

    @PostMapping("/qcEffectivenessPersonnel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcEffectiveness:viewQcOverview",
            tableAlias = "qb"
    )
    public PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> exportQcEffectivenessPersonnel(@RequestBody PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto) {
        return qcEffectivenessService.exportQcEffectivenessPersonnel(dto);
    }

    @PostMapping("/qcReportDetail")
    public PagingVO<ExportQcReportExcelDTO> exportQcReportDetail(@RequestBody PagingDTO<String> dto) {
        return qcReportDetailService.exportQcReportDetail(dto);
    }

    @PostMapping("/soDeliveryNotice")
    @WebAdvanceQuery(handler = SoDeliveryNoticeQueryHandler.class)
    public PagingVO<SoDeliveryNoticeDTO.PagingView> exportSoDeliveryNotice(@RequestBody PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {
        return soDeliveryNoticeService.exportSoDeliveryNotice(dto);
    }

    @PostMapping("/soOutStock")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:paging",
            serviceClass = SoOutstockService.class,
            keyIdName = "so"
    )
    @WebAdvanceQuery(handler = SoOutstockQueryHandler.class)
    public PagingVO<SoOutstockDTO.PagingViewDTO> exportSoOutStock(@RequestBody PagingDTO<SoOutstockDTO.ExportDTO> dto) {
        return soOutstockService.exportSoOutStock(dto);
    }

    @PostMapping("/soReturnInStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soReturnInstock:paging",
            tableAlias = "sri"
    )
    @WebAdvanceQuery(handler = SoReturnInstockQueryHandler.class)
    public PagingVO<SoReturnInstockDTO.PagingView> exportSoReturnInStock(@RequestBody PagingDTO<SoReturnInstockDTO.PagingParam> dto) {
        return soReturnInstockService.exportSoReturnInStock(dto);
    }

    @PostMapping("/soReturnNotice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soReturnNotice:paging",
            tableAlias = "srn"
    )
    @WebAdvanceQuery(handler = SoReturnNoticeQueryHandler.class)
    public PagingVO<SoReturnNoticeDTO.PagingView> exportSoReturnNotice(@RequestBody PagingDTO<SoReturnNoticeDTO.PagingParam> dto) {
       return soReturnNoticeService.exportSoReturnNotice(dto);
    }

    @PostMapping("/soReturnReceive")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:paging",
            tableAlias = "srr"
    )
    @WebAdvanceQuery(handler = SoReturnReceiveQueryHandler.class)
    public PagingVO<SoReturnReceiveDTO.PagingView> exportSoReturnReceive(@RequestBody PagingDTO<SoReturnReceiveDTO.PagingParam> dto) {
        return soReturnReceiveService.exportSoReturnReceive(dto);
    }

    @PostMapping("/stocktakingProfitLoss")
    @WebAdvanceQuery(handler = StocktakingProfitLossQueryHandler.class)
    public PagingVO<StocktakingProfitLossDTO.ExportViewDTO> exportStocktakingProfitLoss(@RequestBody PagingDTO<StocktakingProfitLossDTO.ExportDTO> dto) {
        return stocktakingProfitLossService.exportStocktakingProfitLoss(dto);
    }

    @PostMapping("/stocktakingTaskDetail")
    public PagingVO<StocktakingTaskDetailDTO.ExportDTO> exportStocktakingTaskDetail(@RequestBody PagingDTO<BaseIdDTO> dto) {
        return stocktakingTaskDetailService.exportStocktakingTaskDetail(dto);
    }

    @PostMapping("/subcontractIssue")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = SubcontractIssueQueryHandler.class)
    public PagingVO<SubcontractIssueDTO.ListDTO> exportSubcontractIssue(@RequestBody PagingDTO<SubcontractIssueDTO.PagingParamDTO> dto) {
        return subcontractIssueService.exportSubcontractIssue(dto);
    }

    @PostMapping("/supplierDeliveryOrder")
    @WebAdvanceQuery(handler = SupplierDeliveryQueryHandler.class)
    public PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(@RequestBody PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return supplierDeliveryOrderService.exportSupplierDeliveryOrder(dto);
    }

    @PostMapping("/transferApplication")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:paging",
            tableAlias = "ta"
    )
    @WebAdvanceQuery(handler = TransferApplicationQueryHandler.class)
    public PagingVO<TransferApplicationDTO.ListDTO> exportTransferApplication(@RequestBody PagingDTO<TransferApplicationDTO.SearchParamDTO> dto) {
        return transferApplicationService.exportTransferApplication(dto);
    }

    @PostMapping("/transferInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:paging",
            tableAlias = "ti"
    )
    @WebAdvanceQuery(handler = TransferInfoQueryHandler.class)
    public PagingVO<TransferInfoDTO.ListDTO> exportTransferInfo(@RequestBody PagingDTO<TransferInfoDTO.SearchParamDTO> dto) {
        return transferInfoService.exportTransferInfo(dto);
    }

    @PostMapping("/transferIn")
    @WebAdvanceQuery(handler = TransferInQueryHandler.class)
    public PagingVO<TransferInDTO.PagingViewDTO> exportTransferIn(@RequestBody PagingDTO<TransferInDTO.ExportDTO> dto) {
        return transferInService.exportTransferIn(dto);
    }

    @PostMapping("/transferOut")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transfer:out:export",
            tableAlias = "tfo"
    )
    @WebAdvanceQuery(handler = TransferOutQueryHandler.class)
    public PagingVO<TransferOutDTO.PagingViewDTO> exportTransferOut(@RequestBody PagingDTO<TransferOutDTO.ExportDTO> dto) {
        return transferOutService.exportTransferOut(dto);
    }

    @PostMapping("/virtualTransFlow")
    @WebAdvanceQuery(handler = VirtualTransFlowQueryHandler.class)
    public PagingVO<VirtualTransFlowDTO.ListDTO> exportVirtualTransFlow(@RequestBody PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        return virtualTransFlowService.exportVirtualTransFlow(dto);
    }

    @PostMapping("/warehouse")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:paging",
            tableAlias = "warehouse"
    )
    public PagingVO<WarehouseExportExcelDTO> exportWarehouse(@RequestBody PagingDTO<WarehouseDTO.ExportDTO> dto) {
      return  warehouseService.exportWarehouse(dto);
    }

    @PostMapping("/warehouseLocationMoveInfo")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:export",
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "id")
    @WebAdvanceQuery(handler = MarehouseMoveInfoQueryHandler.class)
    public PagingVO<WarehouseLocationMoveDTO.PdaPcListDTO> exportWarehouseLocationMoveInfo(@RequestBody PagingDTO<WarehouseLocationMoveDTO.ExportDTO> dto) {
        return warehouseLocationMoveService.exportWarehouseLocationMoveInfo(dto);
    }

    @PostMapping("/warehouseLocationReplenish")
    @WebAdvanceQuery(handler = WarehouseLocationReplenishQueryHandler.class)
    public PagingVO<WarehouseLocationReplenishDTO.ViewDTO> exportWarehouseLocationReplenish(@RequestBody PagingDTO<WarehouseLocationReplenishDTO.ExportParamDTO> dto) {
        return warehouseLocationReplenishService.exportWarehouseLocationReplenish(dto);
    }

    @PostMapping("/warehouseReceive")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:warehouseReceive:paging",
            tableAlias = "wr"
    )
    public PagingVO<WarehouseReceiveExportExcelDTO> exportWarehouseReceive(@RequestBody PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto) {
        return warehouseReceiveService.exportWarehouseReceive(dto);
    }

    @PostMapping("/virtualInventory")
    @WebAdvanceQuery
    public PagingVO<VirtualInventoryDTO.ListDTO> getVirtualInventory(@RequestBody PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        return virtualInventoryService.getVirtualInventory(dto);
    }
}
