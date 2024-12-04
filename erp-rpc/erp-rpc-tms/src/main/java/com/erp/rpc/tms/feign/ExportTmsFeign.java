package com.erp.rpc.tms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(name = "erp-tms", contextId = "exportTmsFeign", configuration = ExportFeignConfig.class)
public interface ExportTmsFeign {

    @PostMapping("/feign/export/b2BDeclareBillDeclare")
    PagingVO<TmsDeclareBillDTO.ExportDTO> exportB2BDeclareBillDeclare(@RequestBody PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/b2BDeclareBill")
    PagingVO<TmsDeclareBillDTO.PagingVO> exportB2BDeclareBill(@RequestBody PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/fmDeclareBillDeclare")
    PagingVO<TmsDeclareBillDTO.ExportDTO> exportFmDeclareBillDeclare(@RequestBody PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/fmDeclareBill")
    PagingVO<TmsDeclareBillDTO.PagingVO> exportFmDeclareBill(@RequestBody PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/b2cDeclareReconciliationDetail")
    PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> exportB2cDeclareReconciliationDetail(@RequestBody PagingDTO<TmsB2cDeclareReconciliationDetailDTO.ExportDTO> dto);
    @PostMapping("/feign/export/b2cDeclareReconciliation")
    PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> exportB2cDeclareReconciliation(@RequestBody PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto);
    @PostMapping("/feign/export/cfgReconciliationField")
    PagingVO<CfgReconciliationFieldExportExcelDTO> exportCfgReconciliationField(@RequestBody PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/firstMileReconciliationDetail")
    PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> exportFirstMileReconciliationDetail(@RequestBody PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto);
    @PostMapping("/feign/export/firstMileReconciliation")
    PagingVO<TmsFirstMileReconciliationDTO.ListDTO> exportFirstMileReconciliation(@RequestBody PagingDTO<TmsFirstMileReconciliationDTO.ExportDTO> dto);
    @PostMapping("/feign/export/logisticsAddress")
    PagingVO<LogisticsAddressDTO.PagingViewDTO> exportLogisticsAddress(@RequestBody PagingDTO<LogisticsAddressDTO.ExportDTO> dto);
    @PostMapping("/feign/export/logisticsBillCost")
    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsBillCost(@RequestBody PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/smallBagCostAllocation")
    PagingVO<SmallBagCostAllocationDTO.ListDTO> exportSmallBagCostAllocation(@RequestBody PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsBill")
    PagingVO<LogisticsBillDTO.PagingVO> exportLogisticsBill(@RequestBody PagingDTO<LogisticsBillDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsLastMileCost")
    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(@RequestBody PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsSupplier")
    PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportLogisticsSupplier(@RequestBody PagingDTO<LogisticsSupplierDTO.ExportDTO> dto);
    @PostMapping("/feign/export/productRegistration")
    PagingVO<ProductRegistrationDTO.PagingVO> exportProductRegistration(@RequestBody PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/shippingCalculation")
    PagingVO<ShippingCalculationDTO.ListDTO> exportShippingCalculation(@RequestBody PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/shippingTemplate")
    PagingVO<ShippingTemplateDTO.ListDTO> exportShippingTemplate(@RequestBody PagingDTO<ShippingTemplateDTO.ExportExcelParamDTO> dto);
    @PostMapping("/feign/export/transferDeclare")
    PagingVO<TransferDeclareDTO.ExportListDTO> exportTransferDeclare(@RequestBody PagingDTO<TransferDeclareDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/transferLogisticsSupplier")
    PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportTransferLogisticsSupplier(@RequestBody PagingDTO<TransferLogisticsSupplierDTO.ExportDTO> dto);
    @PostMapping("/feign/export/warehouseMapping")
    PagingVO<TmsWarehouseMappingDTO.ListDTO> exportWarehouseMapping(@RequestBody PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportFirstMileCostAllocation")
    PagingVO<FirstMileCostAllocationDTO.PagingVO> exportFirstMileCostAllocation(@RequestBody PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> params);
    @PostMapping("/feign/export/exportInitFirstMileAllocation")
    PagingVO<InitFirstMileAllocationDTO.PagingVO> exportInitFirstMileAllocation(@RequestBody PagingDTO<InitFirstMileAllocationDTO.PagingParamDTO> params);
    @PostMapping("/feign/export/exportInventorySkuCost")
    PagingVO<InventorySkuCostDTO.PagingVO> exportInventorySkuCost(@RequestBody PagingDTO<InventorySkuCostDTO.PagingParamDTO> params);
    @PostMapping("/feign/export/exportFirstMileEstimatedBill")
    PagingVO<FirstMileEstimatedBillDTO.View> exportFirstMileEstimatedBill(@RequestBody PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto);
    @PostMapping("/feign/export/exportFirstMileWeightAllocation")
    PagingVO<FirstMileWeightAllocationDTO.ViewDTO> exportFirstMileWeightAllocation(@RequestBody @Valid PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/logisticsBill")
    PagingVO<LogisticsLargeDTO.PagingViewDTO> exportLogisticsLarge(@RequestBody PagingDTO<LogisticsLargeDTO.PagingParamDTO> dto);
}
