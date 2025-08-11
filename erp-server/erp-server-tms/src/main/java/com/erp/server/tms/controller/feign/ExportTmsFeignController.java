package com.erp.server.tms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import com.erp.server.tms.query.*;
import com.erp.server.tms.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/feign/export")
public class ExportTmsFeignController {

    @Resource
    private TmsDeclareBillService tmsDeclareBillService;
    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;
    @Resource
    private TmsB2cDeclareReconciliationService tmsB2cDeclareReconciliationService;
    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;
    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;
    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;
    @Resource
    private LogisticsAddressService logisticsAddressService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;
    @Resource
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private ProductRegistrationService productRegistrationService;
    @Resource
    private ShippingCalculationService shippingCalculationService;
    @Resource
    private ShippingTemplateService shippingTemplateService;
    @Resource
    private TransferDeclareService transferDeclareService;
    @Resource
    private TmsWarehouseMappingService tmsWarehouseMappingService;
    @Resource
    private LogisticsLastMileCostService logisticsLastMileCostService;
    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;
    @Resource
    private InitFirstMileAllocationService initFirstMileAllocationService;
    @Resource
    private InventorySkuCostService inventorySkuCostService;
    @Resource
    private FirstMileEstimatedBillService firstMileEstimatedBillService;
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;
    @Resource
    private RemotePostcodeService remotePostcodeService;
    @Resource
    private LogisticsLargeService logisticsLargeService;
    @Resource
    private FirstMileChangeRecordService firstMileChangeRecordService;
    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;

    @Resource
    private DictHsCodeService dictHsCodeService;

    @Resource
    private TmsCfgSailingService tmsCfgSailingService;

    @PostMapping("/b2BDeclareBill")
    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
    PagingVO<TmsDeclareBillDTO.PagingVO> exportB2BDeclareBillDeclare(@RequestBody PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto){
        dto.getParams().setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        return tmsDeclareBillService.export(dto);
    }

    @PostMapping("/b2cDeclareReconciliationDetail")
    public PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> exportB2cDeclareReconciliationDetail(@RequestBody PagingDTO<TmsB2cDeclareReconciliationDetailDTO.ExportDTO> dto) {
        return tmsB2cDeclareReconciliationDetailService.exportB2cDeclareReconciliationDetail(dto);
    }

    @PostMapping("/b2cDeclareReconciliation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:export",
            tableAlias = "tbdr"
    )
    @WebAdvanceQuery(handler = TmsB2cDeclareReconciliationQueryHandler.class)
    public PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> exportB2cDeclareReconciliation(@RequestBody PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto) {
        return tmsB2cDeclareReconciliationService.exportB2cDeclareReconciliation(dto);
    }

    @PostMapping("/cfgReconciliationField")
    @WebAdvanceQuery(handler = CfgReconciliationFieldQueryHandler.class)
    public PagingVO<CfgReconciliationFieldExportExcelDTO> exportCfgReconciliationField(@RequestBody PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto) {
        return cfgReconciliationFieldService.exportCfgReconciliationField(dto);
    }

    @PostMapping("/firstMileReconciliationDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliationDetail:export",
            tableAlias = "tfmrd"
    )
    @WebAdvanceQuery(handler = TmsFirstMileReconciliationQueryHandler.class)
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> exportFirstMileReconciliationDetail(@RequestBody PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto) {
        return tmsFirstMileReconciliationDetailService.exportFirstMileReconciliationDetail(dto);
    }

    @PostMapping("/firstMileReconciliation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:export",
            tableAlias = "tfmr"
    )
    @WebAdvanceQuery(handler = TmsFirstMileReconciliationQueryHandler.class)
    public PagingVO<TmsFirstMileReconciliationDTO.ListDTO> exportFirstMileReconciliation(@RequestBody PagingDTO<TmsFirstMileReconciliationDTO.ExportDTO> dto) {
        return tmsFirstMileReconciliationService.exportFirstMileReconciliation(dto);
    }

    @PostMapping("/logisticsAddress")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:paging",
            tableAlias = "la"
    )
    public PagingVO<LogisticsAddressDTO.PagingViewDTO> exportLogisticsAddress(@RequestBody PagingDTO<LogisticsAddressDTO.ExportDTO> dto) {
        return logisticsAddressService.exportLogisticsAddress(dto);
    }

    @PostMapping("/logisticsBillCost")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsBillCost:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery(handler = LogisticsBillCostQueryHandler.class)
    public PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsBillCost(@RequestBody PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        return logisticsBillCostService.exportLogisticsBillCost(dto);
    }
    @PostMapping("/smallBagCostAllocation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "k.shop_id",
            menuCode = "tms:smallBagCostAllocation:paging",
            tableAlias = "t")
    @WebAdvanceQuery(handler = SmallBagCostAllocationQueryHandler.class)
    public PagingVO<SmallBagCostAllocationDTO.ListDTO> exportSmallBagCostAllocation(@RequestBody PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto) {
    	return smallBagCostAllocationService.paging(dto);
    }
    @PostMapping("/transferDeclareCostAllocation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "j.shop_id",
            menuCode = "tms:transferDeclareCostAllocation:paging",
            tableAlias = "t")
    @WebAdvanceQuery(handler = TransferDeclareCostAllocationQueryHandler.class)
    public PagingVO<TransferDeclareCostAllocationDTO.ListDTO> exportTransferDeclareCostAllocation(@RequestBody PagingDTO<TransferDeclareCostAllocationDTO.PagingParamDTO> dto) {
    	return transferDeclareCostAllocationService.paging(dto);
    }

    @PostMapping("/logisticsBill")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "lb"
    )
    @WebAdvanceQuery(handler = LogisticsBillQueryHandler.class)
    public PagingVO<LogisticsBillDTO.PagingVO> exportLogisticsBill(@RequestBody PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        return logisticsBillService.exportLogisticsBill(dto);
    }

    @PostMapping("/logisticsLastMileCost")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "shop_charge_id",
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsLastMileCost:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery(handler = LogisticsLastMileCostQueryHandler.class)
    public PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(@RequestBody PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        return logisticsLastMileCostService.exportLogisticsLastMileCost(dto);
    }

    @PostMapping("/logisticsSupplier")
    @WebAdvanceQuery(handler = LogisticsSupplierQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsSupplier:paging",
            tableAlias = "ls"
    )
    public PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportLogisticsSupplier(@RequestBody PagingDTO<LogisticsSupplierDTO.ExportDTO> dto) {
        return logisticsSupplierService.exportLogisticsSupplier(dto);
    }

    @PostMapping("/productRegistration")
    @WebAdvanceQuery(handler = ProductRegistrationQueryHandler.class)
    public PagingVO<ProductRegistrationDTO.PagingVO> exportProductRegistration(@RequestBody PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        return productRegistrationService.exportProductRegistration(dto);
    }

    @PostMapping("/shippingCalculation")
    public PagingVO<ShippingCalculationDTO.ListDTO> exportShippingCalculation(@RequestBody PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto) {
        return shippingCalculationService.exportShippingCalculation(dto);
    }

    @PostMapping("/shippingTemplate")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:paging",
            tableAlias = "st"
    )
    public PagingVO<ShippingTemplateDTO.ListDTO> exportShippingTemplate(@RequestBody PagingDTO<ShippingTemplateDTO.ExportExcelParamDTO> dto) {
        return shippingTemplateService.exportShippingTemplate(dto);
    }

    @PostMapping("/transferDeclare")
    @WebAdvanceQuery(handler = TmsTransferDeclareQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:paging",
            tableAlias = "td"
    )
    public PagingVO<TransferDeclareDTO.ExportListDTO> exportTransferDeclare(@RequestBody PagingDTO<TransferDeclareDTO.PagingParamDTO> dto) {
        return transferDeclareService.exportTransferDeclare(dto);
    }

    @PostMapping("/transferLogisticsSupplier")
    public PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> exportTransferLogisticsSupplier(@RequestBody PagingDTO<TransferLogisticsSupplierDTO.ExportDTO> dto) {
        return transferLogisticsSupplierService.exportTransferLogisticsSupplier(dto);
    }

    @PostMapping("/warehouseMapping")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "twm.erp_warehouse_id",
            menuCode = "tms:tmsWarehouseMapping:paging",
            tableAlias = "twm"
    )
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    public PagingVO<TmsWarehouseMappingDTO.ListDTO> exportWarehouseMapping(@RequestBody PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return tmsWarehouseMappingService.exportWarehouseMapping(dto);
    }

    /**
     * 费用分摊
     * @param params
     * @return
     */
    @PostMapping("/exportFirstMileCostAllocation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "a.shop_id",
            warehouseTableField = "a.from_warehouse_id,a.to_warehouse_id",
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public PagingVO<FirstMileCostAllocationDTO.PagingVO> exportFirstMileCostAllocation(@RequestBody PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> params) {
        return firstMileCostAllocationService.paging(params);
    }

    /**
     * 期初头程分摊
     * @param params
     * @return
     */
    @PostMapping("/exportInitFirstMileAllocation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "ad.shop_id",
            warehouseTableField = "ad.warehouse_id",
            menuCode = "tms:initFirstMileAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = InitFirstMileAllocationQueryHandler.class)
    PagingVO<InitFirstMileAllocationDTO.PagingVO> exportInitFirstMileAllocation(@RequestBody PagingDTO<InitFirstMileAllocationDTO.PagingParamDTO> params){
        return initFirstMileAllocationService.paging(params);
    }

    /**
     * sku成本
     * @param params
     * @return
     */
    @PostMapping("/exportInventorySkuCost")
    PagingVO<InventorySkuCostDTO.PagingVO> exportInventorySkuCost(@RequestBody PagingDTO<InventorySkuCostDTO.PagingParamDTO> params){
        return inventorySkuCostService.paging(params);
    }

    /**
     * 暂估账单
     */
    @PostMapping("/exportFirstMileEstimatedBill")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "lb.shop_id",
            menuCode = "tms:firstMileEstimatedBill:paging"
    )
    @WebAdvanceQuery(handler = FirstMileEstimatedQueryHandler.class)
    public PagingVO<FirstMileEstimatedBillDTO.View> exportFirstMileEstimatedBill(@RequestBody PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto){
        return firstMileEstimatedBillService.paging(dto);
    }

    /**
     * 重量分摊
     */
    @PostMapping("/exportFirstMileWeightAllocation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "wa.shop_id",
            warehouseTableField = "wa.from_warehouse_id",
            menuCode = "tms:firstMileWeightAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = FirstMileWeightAllocationQueryHandler.class)
    public PagingVO<FirstMileWeightAllocationDTO.ViewDTO> exportFirstMileWeightAllocation(@RequestBody @Valid PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        return firstMileWeightAllocationService.paging(dto);
    }
    /**
     * 偏远邮编到处
     */
    @PostMapping("/exportRemotePostcode")
    @WebAdvanceQuery
    public PagingVO<RemotePostcodeDTO.ExportListDTO> exportRemotePostcode(@RequestBody @Valid PagingDTO<RemotePostcodeDTO.ExportDTO> dto) {
        return remotePostcodeService.listExport(dto);
    }
    /**
     * 物流大表
     * @param dto
     * @return
     */
    @PostMapping("/exportLogisticsLarge")
    @WebAdvanceQuery(handler = LogisticsLargeQueryHandler.class)
    public PagingVO<LogisticsLargeDTO.PagingViewDTO> exportLogisticsLarge(@RequestBody @Valid PagingDTO<LogisticsLargeDTO.PagingParamDTO> dto) {
        return logisticsLargeService.paging(dto);
    }


    /**
     * 头程报关导出查询
     */
    @PostMapping("/fmDeclareBill")
    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
    public PagingVO<TmsDeclareBillDTO.PagingVO> exportFmDeclareBill(@RequestBody PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto){
        dto.getParams().setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        return tmsDeclareBillService.export(dto);
    }

    /**
     * 头程调整记录导出
     * @param dto
     * @return
     */
    @PostMapping("/exportFirstMileChangeRecord")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:firstMileChangeRecord:paging",
            tableAlias = "fmcr"
    )
    @WebAdvanceQuery(handler = FirstMileChangeRecordQueryHandler.class)
    public PagingVO<FirstMileChangeRecordDTO.PagingVO> exportFirstMileChangeRecord(@RequestBody PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto){
        return firstMileChangeRecordService.paging(dto);
    }

    /**
     * 物流-第三方渠道关系表导出
     * @param dto
     * @return
     */
    @PostMapping("/exportLogisticsThirdChannelRef")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            menuCode = "tms:logisticsThirdChannelRef:paging",
            tableAlias = "ltcr"
    )
    @WebAdvanceQuery(handler = LogisticsThirdChannelRefQueryHandler.class)
    public PagingVO<LogisticsThirdChannelRefDTO.PagingVO> exportLogisticsThirdChannelRef(@RequestBody PagingDTO<LogisticsThirdChannelRefDTO.PagingParamDTO> dto){
        return logisticsThirdChannelRefService.paging(dto);
    }

    /**
     * 出口申报要素导出
     * @param dto
     * @return
     */
    @PostMapping("/exportDictHsCode")
    @WebAdvanceQuery
    public PagingVO<DictHsCodeDTO.ListDTO> exportDictHsCode(@RequestBody  PagingDTO<DictHsCodeDTO.PagingParamDTO> dto){
        return dictHsCodeService.paging(dto);
    }

    @PostMapping("/exportTmsCfgSailing")
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    PagingVO<TmsCfgSailingDTO.ListDTO> exportTmsCfgSailing(@RequestBody PagingDTO<TmsCfgSailingDTO.PagingParamDTO> dto){
        return tmsCfgSailingService.paging(dto);
    }
}
