package com.erp.server.wms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.enums.SettingEnum;
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
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.server.wms.handler.InventoryQueryHandler;
import com.erp.server.wms.query.*;
import com.erp.server.wms.service.*;
import org.springframework.validation.annotation.Validated;
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
    private RequisitionApplicationChangeService requisitionApplicationChangeService;
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
    private SoDeliveryNoticeChangeService soDeliveryNoticeChangeService;
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
    private SubcontractReturnService subcontractReturnService;
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

    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;


    @Resource
    private ReportOrderDemandDetailService reportOrderDemandDetailService;

    @Resource
    private ReportOrderDemandService reportOrderDemandService;

    @Resource
    private ReportOrderSalesService reportOrderSalesService;
    @Resource
    private FbaTransitCalculateReportService fbaTransitCalculateReportService;

    @Resource
    private SoB2bProcessingService soB2bProcessingService;

    @Resource
    private SoB2cProcessingService soB2cProcessingService;

    @Resource
    private FirstMileProcessingService firstMileProcessingService;

    @Resource
    private VirtualInventoryDetailService virtualInventoryDetailService;

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;
    @Resource
    private QcNoticeService qcNoticeService;
    @Resource
    private VirtualAdjustService virtualAdjustService;
    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @Resource
    private SupplierInventoryService supplierInventoryService;

    @PostMapping("/b2cDelivery")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sbdd.warehouse_id",
            shopTableField = "sbd.shop_id",
            menuCode = "wms:soB2cDelivery:paging"
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "wl.warehouse_id",
            menuCode = "wms:warehouseLocation:pagingByParam"
    )
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

    @PostMapping("/requisitionApplicationChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:exportExcel",
            tableAlias = "rac"
    )
    @WebAdvanceQuery
    public PagingVO<RequisitionApplicationChangeDTO.ListDTO> exportRequisitionApplicationChange(@RequestBody PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> dto) {
        return requisitionApplicationChangeService.paging(dto);
    }


    @PostMapping("/warehouseLocationSafetyInventory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "warehouse_id",
            menuCode = "wms:warehouseLocationSafetyInventory:paging"
    )
    @WebAdvanceQuery(handler = WarehouseLocationSafetyInventoryHandler.class)
    public PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> exportWarehouseLocationSafetyInventory(@RequestBody PagingDTO<WarehouseLocationSafetyInventoryDTO.exportParamDTO> dto) {
        return warehouseLocationSafetyInventoryService.exportWarehouseLocationSafetyInventory(dto);
    }

    @PostMapping("/listDiffExportData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "diff.warehouseId",
            menuCode = "wms:virtualInventoryDiff:diffPaging"
    )
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "vmad.warehouse_id",
            menuCode = "wms:virtualWarehouseAllocation:paging",
            tableAlias = "vma"
    )
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
            shopTableField = "fd.shop_id",
            warehouseTableField = "fd.delivery_warehouse_id,fd.dest_warehouse_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = "fd"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<FirstMileDeliveryDTO.ListDTO> exportFbaDelivery(@RequestBody PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        return firstMileDeliveryService.exportFbaDelivery(dto);
    }

    @PostMapping("/getInventoryPageData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "it.warehouse_id",
            menuCode = "wms:inventory:paging"
    )
    @WebAdvanceQuery(handler = WmsInventoryQueryHandler.class)
    PagingVO<InventoryDTO.PagingViewDTO> getInventoryPageData(@RequestBody PagingDTO<InventoryDTO.ExportSearchParamDTO> dto) {
        return inventoryService.getInventoryPageData(dto);
    }

    @PostMapping("/dailyQcBill")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            warehouseTableField = "qb.warehouse_id",
            menuCode = "wms:qcBill:exportQcBill",
            tableAlias = "qb")
    @WebAdvanceQuery(handler = QcInfoQueryHandler.class)
    public PagingVO<QcInfoDTO.QcDailyReportDTO> exportDailyQcBill(@RequestBody PagingDTO<QcInfoDTO.ExportDTO> dto) {
        return qcInfoService.exportDailyQcBill(dto);
    }

    @PostMapping("/fbaInventory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "fi.warehouse_id",
            menuCode = "wms:fbaInventory:export",
            tableAlias = "fi"
    )
    @WebAdvanceQuery
    public PagingVO<FbaInventoryDTO.ListDTO> exportFbaInventory(@RequestBody PagingDTO<FbaInventoryDTO.ExportDTO> dto) {
        return fbaInventoryService.exportFbaInventory(dto);
    }

    @PostMapping("/fbaShipment")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "fs.shop_id",
            menuCode = "wms:fbaShipment:paging"
    )
    @WebAdvanceQuery
    public PagingVO<FbaShipmentDTO.ExportDTO> exportFbaShipment(@RequestBody PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        return fbaShipmentService.exportFbaShipment(dto);
    }

    @PostMapping("/fbaShipmentPacking")
    @WebAdvanceQuery
    public PagingVO<FbaShipmentPackingDTO.ViewDTO> exportFbaShipmentPacking(@RequestBody PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        return fbaShipmentPackingService.exportFbaShipmentPacking(dto);
    }

    @PostMapping("/initStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "ism.warehouse_id",
            menuCode = "wms:initStock:paging",
            tableAlias = "ism"
    )
    @WebAdvanceQuery
    public PagingVO<InitStockDTO.ListDTO> exportInitStock(@RequestBody PagingDTO<InitStockDTO.ExportSearchParamDTO> dto) {
        return initStockService.exportInitStock(dto);
    }

    @PostMapping("/inventoryDaily")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "tf.warehouse_id",
            menuCode = "wms:inventory:dailyInventoryPaging"
    )
    @WebAdvanceQuery(handler = InventoryQueryHandler.class)
    public PagingVO<InventoryReportDTO.ListDailyInventoryDTO> exportInventoryDaily(@RequestBody PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto) {
        return transactionFlowService.exportInventoryDaily(dto);
    }

    @PostMapping("/inventoryInOutStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "t.warehouse_id",
            menuCode = "wms:inventory:pageInOutStock"
    )
    @WebAdvanceQuery(handler = InventoryQueryHandler.class)
    public PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> exportInventoryInOutStock(@RequestBody PagingDTO<InventoryDTO.ExportInOutStockTransFlowSearchParamDTO> dto) {
        return transactionFlowService.exportInventoryInOutStock(dto);
    }

    @PostMapping("/inOutStockSummary")
    public PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> exportInOutStockSummary(@RequestBody PagingDTO<InventoryDTO.ExcelInOutStockSummarySearchParamDTO> dto) {
        return transactionFlowService.exportInOutStockSummary(dto);
    }

    @PostMapping("/inventoryTransFlow")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "t.warehouse_id",
            menuCode = "wms:inventory:paging"
    )
    @WebAdvanceQuery(handler = WmsInventoryQueryHandler.class)
    public PagingVO<InventoryDTO.TransFlowPagingViewDTO> exportInventoryTransFlow(@RequestBody PagingDTO<InventoryDTO.ExportInvFlowSearchParamDTO> dto) {
        return transactionFlowService.exportInventoryTransFlow(dto);
    }

    @PostMapping("/inventoryTransport")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "tf.warehouse_id",
            menuCode = "wms:inventory:transport:paging"
    )
    @WebAdvanceQuery(handler = InventoryQueryHandler.class)
    public PagingVO<InventoryReportDTO.TransportPagingDTO> exportInventoryTransport(@RequestBody PagingDTO<InventoryReportDTO.ExportTransportSearchParamDTO> dto) {
        return transactionFlowService.exportInventoryTransport(dto);
    }

    @PostMapping("/machineInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            warehouseTableField = "mi.warehouse_id",
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
            warehouseTableField = "oi.warehouse_id",
            menuCode = "wms:otherInstock:paging",
            tableAlias = "oi"
    )
    @WebAdvanceQuery(handler = OtherInstockQueryHandler.class)
    public PagingVO<OtherInstockDTO.ListDTO> exportOtherInStock(@RequestBody PagingDTO<OtherInstockDTO.SearchParamDTO> dto) {
        return otherInstockService.exportOtherInStock(dto);
    }

    @PostMapping("/otherOutStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id,create_user_id",
            warehouseTableField = "oo.warehouse_id",
            menuCode = "wms:otherOutstock:paging",
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
            warehouseTableField = "opw.warehouse_id",
            menuCode = "wms:overseasInventory:exportExcel",
            tableAlias = "op")
    @WebAdvanceQuery(handler = OverseasInventoryQueryHandler.class)
    public PagingVO<OverseasInventoryDTO.ListDTO> exportOverseasInventory(@RequestBody PagingDTO<OverseasInventoryDTO.ExportDTO> dto) {
        return overseasInventoryService.exportOverseasInventory(dto);
    }

    @PostMapping("/overseasWarehouseInbound")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "owi.delivery_warehouse_id,owi.to_warehouse_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            tableAlias = "owi"
    )
    @WebAdvanceQuery
    public PagingVO<OverseasWarehouseInboundDTO.ListDTO> exportOverseasWarehouseInbound(@RequestBody PagingDTO<OverseasWarehouseInboundDTO.ExportDTO> dto) {
        return overseasWarehouseInboundService.exportOverseasWarehouseInbound(dto);
    }

    @PostMapping("/packingTaskDetail")
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        return packingTaskService.exportPackingTaskDetail(dto);
    }
    @PostMapping("/exportPackingTaskDetailMerge")
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetailMerge(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        return packingTaskService.exportPackingTaskDetailMerge(dto);
    }
    @PostMapping("/unPackingTaskDetail")
    public PagingVO<WmsCartonSpecDTO.NoPackingViewDTO> unPackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        return packingTaskService.unPackingTaskDetail(dto);
    }

    @PostMapping("/firstMilePackingTaskDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = "fd"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> firstMilePackingTaskDetail(@RequestBody PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        return firstMileDeliveryService.firstMilePackingTaskDetail(dto);
    }

    @PostMapping("/packingTask")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "pt.warehouse_id",
            menuCode = "wms:packingTask:exportPacking",
            tableAlias = "pt"
    )

    @WebAdvanceQuery(handler = PackingTaskQueryHandler.class)
    public PagingVO<PackingTaskDTO.PagingViewDTO> exportPackingTask(@RequestBody PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        return packingTaskService.exportPackingTask(dto);
    }

    @PostMapping("/pickingLists")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "pl.warehouse_id",
            menuCode = "wms:picking-lists:paging",
            tableAlias = "pl"
    )
    @WebAdvanceQuery
    public PagingVO<PickingListsDTO.ExportInfoDTO> exportPickingLists(@RequestBody PagingDTO<PickingListsDTO.ExportDTO> dto) {
        return pickingListsService.exportPickingLists(dto);
    }

    @PostMapping("/poInStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            warehouseTableField = "psi.delivery_warehouse_id",
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
            warehouseTableField = "pro.return_warehouse_id",
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
            warehouseTableField = "qb.warehouse_id",
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
    public PagingVO<ExportQcReportExcelDTO> exportQcReportDetail(@RequestBody PagingDTO<BaseIdDTO> dto) {
        return qcReportDetailService.exportQcReportDetail(dto);
    }

    @PostMapping("/soDeliveryNotice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sdn.warehouse_id",
            menuCode = "wms:soDeliveryNotice:paging",
            tableAlias = "sdn"
    )
    @WebAdvanceQuery(handler = SoDeliveryNoticeQueryHandler.class)
    public PagingVO<SoDeliveryNoticeDTO.PagingView> exportSoDeliveryNotice(@RequestBody PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {
        return soDeliveryNoticeService.exportSoDeliveryNotice(dto);
    }

    @PostMapping("/soDeliveryNoticeChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sdn.warehouse_id",
            menuCode = "wms:soDeliveryNoticeChange:paging",
            tableAlias = "sdnc"
    )
    @WebAdvanceQuery
    public PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> exportSoDeliveryNoticeChange(@RequestBody PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> dto) {
        return soDeliveryNoticeChangeService.paging(dto);
    }

    @PostMapping("/soOutStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            warehouseTableField = "so.warehouse_id",
            shopTableField = "so.shop_id",
            menuCode = "wms:so:outstock:paging",
            tableAlias = "so"
    )
    @WebAdvanceQuery(handler = SoOutstockQueryHandler.class)
    public PagingVO<SoOutstockDTO.PagingViewDTO> exportSoOutStock(@RequestBody PagingDTO<SoOutstockDTO.ExportDTO> dto) {
    	PagingVO<SoOutstockDTO.PagingViewDTO> pagingVO = soOutstockService.exportSoOutStock(dto);
        return pagingVO;
    }

    @PostMapping("/soReturnInStock")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "srid.warehouse_id",
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
            warehouseTableField = "srn.warehouse_id",
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
            warehouseTableField = "srr.warehouse_id",
            menuCode = "wms:soReturnReceive:paging",
            tableAlias = "srr"
    )
    @WebAdvanceQuery(handler = SoReturnReceiveQueryHandler.class)
    public PagingVO<SoReturnReceiveDTO.PagingView> exportSoReturnReceive(@RequestBody PagingDTO<SoReturnReceiveDTO.PagingParam> dto) {
        return soReturnReceiveService.exportSoReturnReceive(dto);
    }

    @PostMapping("/stocktakingProfitLoss")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "spld.warehouse_id",
            menuCode = "wms:stocktakingProfitLoss:paging",
            tableAlias = "spl"
    )
    @WebAdvanceQuery(handler = StocktakingProfitLossQueryHandler.class)
    public PagingVO<StocktakingProfitLossDTO.ExportViewDTO> exportStocktakingProfitLoss(@RequestBody PagingDTO<StocktakingProfitLossDTO.ExportDTO> dto) {
        return stocktakingProfitLossService.exportStocktakingProfitLoss(dto);
    }

    @PostMapping("/stocktakingTaskDetail")
    public PagingVO<StocktakingTaskDetailDTO.ExportDTO> exportStocktakingTaskDetail(@RequestBody PagingDTO<StocktakingTaskDTO.BaseIdDTO> dto) {
        return stocktakingTaskDetailService.exportStocktakingTaskDetail(dto);
    }

    @PostMapping("/subcontractIssue")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sid.warehouse_id",
            menuCode = "wms:subcontractIssue:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = SubcontractIssueQueryHandler.class)
    public PagingVO<SubcontractIssueDTO.ListDTO> exportSubcontractIssue(@RequestBody PagingDTO<SubcontractIssueDTO.PagingParamDTO> dto) {
        return subcontractIssueService.exportSubcontractIssue(dto);
    }
    @PostMapping("/subcontractReturn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "srd.warehouse_id",
            menuCode = "wms:subcontractReturn:export",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SubcontractReturnQueryHandler.class)
    public PagingVO<SubcontractReturnDTO.ListDTO> exportSubcontractReturn(@RequestBody PagingDTO<SubcontractReturnDTO.PagingParamDTO> dto){
        return subcontractReturnService.paging(dto);
    }

    @PostMapping("/supplierDeliveryOrder")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "do2.to_warehouse_id",
            menuCode = "wms:supplierDeliveryOrder:paging"
    )
    @WebAdvanceQuery(handler = SupplierDeliveryQueryHandler.class)
    public PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(@RequestBody PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return supplierDeliveryOrderService.exportSupplierDeliveryOrder(dto);
    }

    @PostMapping("/transferApplication")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            warehouseTableField = "ta.in_warehouse_id,ta.out_warehouse_id",
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
            warehouseTableField = "tid.in_warehouse_id,tid.out_warehouse_id",
            menuCode = "wms:transferInfo:paging",
            tableAlias = "ti"
    )
    @WebAdvanceQuery(handler = TransferInfoQueryHandler.class)
    public PagingVO<TransferInfoDTO.ListDTO> exportTransferInfo(@RequestBody PagingDTO<TransferInfoDTO.SearchParamDTO> dto) {
        return transferInfoService.exportTransferInfo(dto);
    }

    @PostMapping("/transferIn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "ti.out_warehouse_id,ti.in_warehouse_id",
            menuCode = "wms:transfer:in:paging",
            tableAlias = "ti"
    )
    @WebAdvanceQuery(handler = TransferInQueryHandler.class)
    public PagingVO<TransferInDTO.PagingViewDTO> exportTransferIn(@RequestBody PagingDTO<TransferInDTO.ExportDTO> dto) {
        return transferInService.exportTransferIn(dto);
    }

    @PostMapping("/transferOut")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "tfo.out_warehouse_id",
            menuCode = "wms:transfer:out:export",
            tableAlias = "tfo"
    )
    @WebAdvanceQuery(handler = TransferOutQueryHandler.class)
    public PagingVO<TransferOutDTO.PagingViewDTO> exportTransferOut(@RequestBody PagingDTO<TransferOutDTO.ExportDTO> dto) {
        return transferOutService.exportTransferOut(dto);
    }

    @PostMapping("/virtualTransFlow")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vtf.warehouse_id",
            menuCode = "wms:virtualTransFlow:paging"
    )
    @WebAdvanceQuery(handler = VirtualTransFlowQueryHandler.class)
    public PagingVO<VirtualTransFlowDTO.ListDTO> exportVirtualTransFlow(@RequestBody PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        return virtualTransFlowService.exportVirtualTransFlow(dto);
    }

    @PostMapping("/warehouse")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "id",
            menuCode = "wms:warehouse:paging",
            tableAlias = "warehouse"
    )
    public PagingVO<WarehouseExportExcelDTO> exportWarehouse(@RequestBody PagingDTO<WarehouseDTO.ExportDTO> dto) {
      return  warehouseService.exportWarehouse(dto);
    }

    @PostMapping("/warehouseLocationMoveInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "wlmd.warehouse_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:paging",
            tableAlias = "wlmi"
    )
    @WebAdvanceQuery(handler = MarehouseMoveInfoQueryHandler.class)
    public PagingVO<WarehouseLocationMoveDTO.PdaPcListDTO> exportWarehouseLocationMoveInfo(@RequestBody PagingDTO<WarehouseLocationMoveDTO.ExportDTO> dto) {
        return warehouseLocationMoveService.exportWarehouseLocationMoveInfo(dto);
    }

    @PostMapping("/warehouseLocationReplenish")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "warehouse_id",
            menuCode = "wms:warehouseLocationReplenish:paging"
    )
    @WebAdvanceQuery(handler = WarehouseLocationReplenishQueryHandler.class)
    public PagingVO<WarehouseLocationReplenishDTO.ViewDTO> exportWarehouseLocationReplenish(@RequestBody PagingDTO<WarehouseLocationReplenishDTO.ExportParamDTO> dto) {
        return warehouseLocationReplenishService.exportWarehouseLocationReplenish(dto);
    }

    @PostMapping("/warehouseReceive")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            warehouseTableField = "wr.delivery_warehouse_id",
            menuCode = "wms:warehouseReceive:paging",
            tableAlias = "wr"
    )
    @WebAdvanceQuery(handler = WarehouseReceiveQueryHandler.class)
    public PagingVO<WarehouseReceiveExportExcelDTO> exportWarehouseReceive(@RequestBody PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto) {
        return warehouseReceiveService.exportWarehouseReceive(dto);
    }

    @PostMapping("/virtualInventory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vi.warehouse_id",
            menuCode = "wms:virtualInventory:paging"
    )
    @WebAdvanceQuery
    public PagingVO<VirtualInventoryDTO.ListDTO> getVirtualInventory(@RequestBody PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        return virtualInventoryService.getVirtualInventory(dto);
    }

    @PostMapping("/inventoryAge")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "inv.warehouse_id",
            menuCode = "wms:inventory:inventoryAge:paging"
    )
    @WebAdvanceQuery(handler = InventoryQueryHandler.class)
    public PagingVO<DynamicExcelDTO> exportWmsInventoryAge(@RequestBody PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto){
        return inventoryService.exportWmsInventoryAge(dto);
    }

    /**
     * 订单需求明细导出
     */
    @PostMapping("/listReportOrderDemandDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "rodd.warehouse_id",
            menuCode = "wms:reportOrderDemandDetail:paging"
    )
    @WebAdvanceQuery
    public PagingVO<ReportOrderDemandDetailDTO.ListDTO> listReportOrderDemandDetail(@RequestBody PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> dto){
        return reportOrderDemandDetailService.listReportOrderDemandDetail(dto);
    }
    /**
     * 缺货统计数据导出
     */
    @PostMapping("/listReportOrderDemand")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "rod.warehouse_id",
            menuCode = "wms:reportOrderDemand:paging"
    )
    @WebAdvanceQuery
    public PagingVO<ReportOrderDemandDTO.ListDTO> listReportOrderDemand(@RequestBody PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto){
        return reportOrderDemandService.listReportOrderDemand(dto);
    }
    /**
     * 销售看板数据导出
     */
    @PostMapping("/listReportOrderSales")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "ros.warehouse_id",
            menuCode = "wms:reportOrderSales:paging"
    )
    @WebAdvanceQuery
    public PagingVO<ReportOrderSalesDTO.ListDTO> listReportOrderSales(@RequestBody PagingDTO<ReportOrderSalesDTO.PagingParamDTO> dto){
        return reportOrderSalesService.listReportOrderSales(dto);
    }

    /**
     * 导出分货统计
     * @author will
     * @date 2024/11/19 17:43
     * @param dto
     * @return PagingVO<ExportStatisticsDTO>
     */
    @PostMapping("/exportVirtualStatistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "vmad.warehouse_id",
            menuCode = "wms:virtualWarehouseAllocation:paging",
            tableAlias = "vma"
    )
    @WebAdvanceQuery(handler = VirtualWarehouseAllocationQueryHandler.class)
    public PagingVO<VirtualWarehouseAllocationDTO.ExportStatisticsDTO> exportVirtualStatistics(@RequestBody PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto) {
        return virtualWarehouseAllocationService.exportVirtualStatistics(dto);
    }

    /**
     * 导出b2b销售订单虚拟仓订单跟踪
     */
    @PostMapping("/exportSoB2bProcessing")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sbp.warehouse_id",
            menuCode = "wms:soB2bProcessing:paging"
    )
    @WebAdvanceQuery
    public PagingVO<SoB2bProcessingDTO.ListDTO> exportSoB2bProcessing(@RequestBody PagingDTO<SoB2bProcessingDTO.PagingParamDTO> dto){
        return soB2bProcessingService.paging(dto);
    }

    /**
     * 导出b2c销售订单虚拟仓订单跟踪
     */
    @PostMapping("/exportSoB2cProcessing")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sbp.warehouse_id",
            shopTableField = "sb2c.shop_id",
            menuCode = "wms:soB2cProcessing:paging"
    )
    @WebAdvanceQuery
    public PagingVO<SoB2cProcessingDTO.ListDTO> exportSoB2cProcessing(@RequestBody PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto){
        return soB2cProcessingService.paging(dto);
    }

    /**
     * 导出头程销售订单虚拟仓订单跟踪
     */
    @PostMapping("/exportFirstMileProcessing")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "fmp.warehouse_id",
            menuCode = "wms:firstMileProcessing:paging"
    )
    @WebAdvanceQuery(handler = FirstMileProcessingQueryHandler.class)
    public PagingVO<FirstMileProcessingDTO.ListDTO> exportFirstMileProcessing(@RequestBody PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto){
        return firstMileProcessingService.exportPaging(dto);
    }

    /**
     * 库龄分析数据导出
     */
    @PostMapping("/exportWmsVirtualInventoryAge")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vidh.warehouse_id",
            menuCode = "wms:virtualInventoryAge:paging"
    )
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public PagingVO<DynamicExcelDTO> exportWmsVirtualInventoryAge(@RequestBody PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto){
        return virtualInventoryDetailService.exportWmsVirtualInventoryAge(dto);
    }

    /**
     * 历史库龄数据导出
     */
    @PostMapping("/hisInventoryAgePaging")
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(@RequestBody PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto){
        return virtualInventoryDetailService.hisInventoryAgePaging(dto);
    }

    /**
     * 历史库龄明细数据导出
     */
    @PostMapping("/hisInventoryAgeDetailPaging")
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(@RequestBody PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto){
        return virtualInventoryDetailService.exportHisInventoryAgeDetailPaging(dto);
    }


    /**
     * 流水明细数据导出
     */
    @PostMapping("/virtualTransFlowDetailPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vm.warehouseId",
            menuCode = "wms:virtualTransFlowDetail:paging"
    )
    @WebAdvanceQuery
    public PagingVO<VirtualTransFlowDetailDTO.ListDTO> virtualTransFlowDetailPaging(@RequestBody PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto){
        return virtualTransFlowDetailService.paging(dto);
    }

    /**
     * 列表历史库龄明细数据导出
     */
    @PostMapping("/framePaging")
    @WebAdvanceQuery
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> framePaging(@RequestBody PagingDTO<VirtualInventoryAgeDTO.FrameParamDTO> dto){
        return virtualInventoryDetailService.framePaging(dto);
    }

    /**
     * 导出FBA在途核对列表
     */
    @PostMapping("/exportFbaTransitReport")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "ftcr.warehouse_id",
            menuCode = "wms:fbaTransitCalculateReport:paging"
    )
    @WebAdvanceQuery
    public PagingVO<FbaTransitCalculateReportDTO.ListDTO> exportFbaTransitReport(@RequestBody PagingDTO<FbaTransitCalculateReportDTO.PagingParamDTO> dto){
        return fbaTransitCalculateReportService.paging(dto);
    }

    /**
     * 导出虚拟仓库设置
     */
    @PostMapping("/exportVirtualWarehouse")
    @WebAdvanceQuery
    public PagingVO<VirtualWarehouseDTO.ExportDTO> exportVirtualWarehouse(@RequestBody PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto){
        return virtualWarehouseService.exportVirtualWarehouse(dto);
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-04-21
     * @param dto
     * @return
     */
    @PostMapping("/exportQcNotice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:export",
            tableAlias = "qn"
    )
    @WebAdvanceQuery(handler = QcNoticeQueryHandler.class)
    public PagingVO<QcNoticeDTO.ListDTO> exportList(@RequestBody PagingDTO<QcNoticeDTO.ExportDTO> dto) {
        return qcNoticeService.exportList(dto);
    }

    /**
     * 导出虚拟库存调整
     */
    @PostMapping("/exportVirtualAdjust")
    @WebAdvanceQuery(handler = VirtualAdjustQueryHandler.class)
    PagingVO<VirtualAdjustDTO.ListDTO> exportVirtualAdjust(@RequestBody PagingDTO<VirtualAdjustDTO.PagingParamDTO> dto){
        return virtualAdjustService.paging(dto);
    }

    /**
     * 导出数据查询
     * @author will
     * @date 2025/6/19 11:03
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/supplierInventory")
    @WebAdvanceQuery(handler = SupplierInventoryQueryHandler.class)
    public PagingVO<SupplierInventoryDTO.ListDTO> exportSupplierInventory(@RequestBody PagingDTO<SupplierInventoryDTO.PagingParamDTO> dto) {
        return supplierInventoryService.paging(dto);
    }

    @PostMapping("/exportThirdWarehouseDelivery")
    @WebAdvanceQuery
    public PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> exportThirdWarehouseDelivery(@RequestBody PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto) {
        return thirdWarehouseDeliveryService.paging(dto);
    }

}
