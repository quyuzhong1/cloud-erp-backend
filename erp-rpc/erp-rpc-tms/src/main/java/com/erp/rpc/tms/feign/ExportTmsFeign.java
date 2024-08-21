package com.erp.rpc.tms.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "erp-tms", contextId = "exportTmsFeign")
public interface ExportTmsFeign {

    @PostMapping("/feign/export/b2BDeclareBillDeclare")
    PagingVO<TmsDeclareBillDTO.ExportDTO> exportB2BDeclareBillDeclare(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/b2BDeclareBill")
    PagingVO<TmsDeclareBillDTO.PagingVO> exportB2BDeclareBill(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/fmDeclareBillDeclare")
    PagingVO<TmsDeclareBillDTO.ExportDTO> exportFmDeclareBillDeclare(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/fmDeclareBill")
    PagingVO<TmsDeclareBillDTO.PagingVO> exportFmDeclareBill(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/b2cDeclareReconciliationDetail")
    PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> exportB2cDeclareReconciliationDetail(PagingDTO<TmsB2cDeclareReconciliationDetailDTO.ExportDTO> dto);
    @PostMapping("/feign/export/b2cDeclareReconciliation")
    PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> exportB2cDeclareReconciliation(PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto);
    @PostMapping("/feign/export/cfgReconciliationField")
    PagingVO<CfgReconciliationFieldExportExcelDTO> exportCfgReconciliationField(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/firstMileReconciliationDetail")
    PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> exportFirstMileReconciliationDetail(PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto);
    @PostMapping("/feign/export/firstMileReconciliation")
    PagingVO<TmsFirstMileReconciliationDTO.ListDTO> exportFirstMileReconciliation(PagingDTO<TmsFirstMileReconciliationDTO.ExportDTO> dto);
    @PostMapping("/feign/export/logisticsAddress")
    PagingVO<LogisticsAddressDTO.PagingViewDTO> exportLogisticsAddress(PagingDTO<LogisticsAddressDTO.ExportDTO> dto);
    @PostMapping("/feign/export/logisticsBillCost")
    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsBillCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsBill")
    PagingVO<LogisticsBillDTO.PagingVO> exportLogisticsBill(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsLastMileCost")
    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsSupplier")
    PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportLogisticsSupplier(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto);
    @PostMapping("/feign/export/productRegistration")
    PagingVO<ProductRegistrationDTO.PagingVO> exportProductRegistration(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/shippingCalculation")
    PagingVO<ShippingCalculationDTO.ListDTO> exportShippingCalculation(PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/shippingTemplate")
    PagingVO<ShippingTemplateDTO.ListDTO> exportShippingTemplate(PagingDTO<ShippingTemplateDTO.ExportExcelParamDTO> dto);
    @PostMapping("/feign/export/transferDeclare")
    PagingVO<TransferDeclareDTO.ExportListDTO> exportTransferDeclare(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/transferLogisticsSupplier")
    PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportTransferLogisticsSupplier(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto);
    @PostMapping("/feign/export/warehouseMapping")
    PagingVO<TmsWarehouseMappingDTO.ListDTO> exportWarehouseMapping(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto);
}
