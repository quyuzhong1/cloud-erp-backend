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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @PostMapping("/b2BDeclareBillDeclare")
    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
    PagingVO<TmsDeclareBillDTO.ExportDTO> exportB2BDeclareBillDeclare(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto){
        dto.getParams().setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        return tmsDeclareBillService.exportDeclareBillDeclare(dto);
    }
    @PostMapping("/b2BDeclareBill")
    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
    PagingVO<TmsDeclareBillDTO.PagingVO> exportB2BDeclareBill(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto){
        dto.getParams().setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        return tmsDeclareBillService.exportDeclareBill(dto);
    }
    @PostMapping("/fmDeclareBillDeclare")
    @WebAdvanceQuery(handler = TmsFmDeclareQueryHandler.class)
    PagingVO<TmsDeclareBillDTO.ExportDTO> exportFmDeclareBillDeclare(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto){
        dto.getParams().setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        return tmsDeclareBillService.exportDeclareBillDeclare(dto);
    }
    @PostMapping("/fmDeclareBill")
    @WebAdvanceQuery(handler = TmsFmDeclareQueryHandler.class)
    PagingVO<TmsDeclareBillDTO.PagingVO> exportFmDeclareBill(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto){
        dto.getParams().setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        return tmsDeclareBillService.exportDeclareBill(dto);
    }

    @PostMapping("/b2cDeclareReconciliationDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:export",
            tableAlias = "tbdr"
    )
    @WebAdvanceQuery(handler = TmsB2cDeclareReconciliationQueryHandler.class)
    public PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> exportB2cDeclareReconciliationDetail(PagingDTO<TmsB2cDeclareReconciliationDetailDTO.ExportDTO> dto) {
        return tmsB2cDeclareReconciliationDetailService.exportB2cDeclareReconciliationDetail(dto);
    }

    @PostMapping("/b2cDeclareReconciliation")
    public PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> exportB2cDeclareReconciliation(PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto) {
        return tmsB2cDeclareReconciliationService.exportB2cDeclareReconciliation(dto);
    }

    @PostMapping("/cfgReconciliationField")
    @WebAdvanceQuery(handler = CfgReconciliationFieldQueryHandler.class)
    public PagingVO<CfgReconciliationFieldExportExcelDTO> exportCfgReconciliationField(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto) {
        return cfgReconciliationFieldService.exportCfgReconciliationField(dto);
    }

    @PostMapping("/firstMileReconciliationDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliationDetail:export",
            tableAlias = "tfmrd"
    )
    @WebAdvanceQuery(handler = TmsFirstMileReconciliationQueryHandler.class)
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> exportFirstMileReconciliationDetail(PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto) {
        return tmsFirstMileReconciliationDetailService.exportFirstMileReconciliationDetail(dto);
    }

    @PostMapping("/firstMileReconciliation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:export",
            tableAlias = "tfmr"
    )
    @WebAdvanceQuery(handler = TmsFirstMileReconciliationQueryHandler.class)
    public PagingVO<TmsFirstMileReconciliationDTO.ListDTO> exportFirstMileReconciliation(PagingDTO<TmsFirstMileReconciliationDTO.ExportDTO> dto) {
        return tmsFirstMileReconciliationService.exportFirstMileReconciliation(dto);
    }

    @PostMapping("/logisticsAddress")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:paging",
            tableAlias = "la"
    )
    public PagingVO<LogisticsAddressDTO.PagingViewDTO> exportLogisticsAddress(PagingDTO<LogisticsAddressDTO.ExportDTO> dto) {
        return logisticsAddressService.exportLogisticsAddress(dto);
    }

    @PostMapping("/logisticsBillCost")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBillCost:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery(handler = LogisticsBillCostQueryHandler.class)
    public PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsBillCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        return logisticsBillCostService.exportLogisticsBillCost(dto);
    }

    @PostMapping("/logisticsBill")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "lb"
    )
    @WebAdvanceQuery(handler = LogisticsBillQueryHandler.class)
    public PagingVO<LogisticsBillDTO.PagingVO> exportLogisticsBill(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        return logisticsBillService.exportLogisticsBill(dto);
    }

    @PostMapping("/logisticsLastMileCost")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "shop_charge_id",
            menuCode = "tms:logisticsLastMileCost:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery(handler = LogisticsLastMileCostQueryHandler.class)
    public PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        return logisticsLastMileCostService.exportLogisticsLastMileCost(dto);
    }

    @PostMapping("/logisticsSupplier")
    public PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportLogisticsSupplier(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto) {
        return logisticsSupplierService.exportLogisticsSupplier(dto);
    }

    @PostMapping("/productRegistration")
    @WebAdvanceQuery(handler = ProductRegistrationQueryHandler.class)
    public PagingVO<ProductRegistrationDTO.PagingVO> exportProductRegistration(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        return productRegistrationService.exportProductRegistration(dto);
    }

    @PostMapping("/shippingCalculation")
    public PagingVO<ShippingCalculationDTO.ListDTO> exportShippingCalculation(PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto) {
        return shippingCalculationService.exportShippingCalculation(dto);
    }

    @PostMapping("/shippingTemplate")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:paging",
            tableAlias = "st"
    )
    public PagingVO<ShippingTemplateDTO.ListDTO> exportShippingTemplate(PagingDTO<ShippingTemplateDTO.ExportExcelParamDTO> dto) {
        return shippingTemplateService.exportShippingTemplate(dto);
    }

    @PostMapping("/transferDeclare")
    @WebAdvanceQuery(handler = TmsTransferDeclareQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:paging",
            tableAlias = "td"
    )
    public PagingVO<TransferDeclareDTO.ExportListDTO> exportTransferDeclare(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto) {
        return transferDeclareService.exportTransferDeclare(dto);
    }

    @PostMapping("/transferLogisticsSupplier")
    public PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> exportTransferLogisticsSupplier(PagingDTO<TransferLogisticsSupplierDTO.ExportDTO> dto) {
        return transferLogisticsSupplierService.exportTransferLogisticsSupplier(dto);
    }

    @PostMapping("/warehouseMapping")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsWarehouseMapping:paging",
            tableAlias = "twm"
    )
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    public PagingVO<TmsWarehouseMappingDTO.ListDTO> exportWarehouseMapping(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return tmsWarehouseMappingService.exportWarehouseMapping(dto);
    }
}
