package com.erp.rpc.scm.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.*;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-scm", contextId = "exportScmFeign", configuration = ExportFeignConfig.class)
public interface ExportScmFeign {

    @PostMapping("/feign/export/purchaseApplication")
    PagingVO<PurchaseApplicationDTO.ListDTO> exportPurchaseApplication(@RequestBody PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto);
    @PostMapping("/feign/export/purchaseChange")
    PagingVO<PurchaseChangeExportExcelDTO> exportPurchaseChange(@RequestBody PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto);
    @PostMapping("/feign/export/purchaseOrderContract")
    PagingVO<BomExportExcelVO> exportPurchaseOrderContract(@RequestBody PagingDTO<String> dto);
    @PostMapping("/feign/export/purchaseOrder")
    PagingVO<PurchaseOrderDTO.ListDTO> exportPurchaseOrder(@RequestBody PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto);
    @PostMapping("/feign/export/purchasePriceChange")
    PagingVO<PurchasePriceChangeExportExcelDTO> exportPurchasePriceChange(@RequestBody PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/purchasePrice")
    PagingVO<PurchasePriceExportExcelDTO> exportPurchasePrice(@RequestBody PagingDTO<PurchasePriceDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/salesDemand")
    PagingVO<SalesDemandExportExcelDTO> exportSalesDemand(@RequestBody PagingDTO<SalesDemandDTO.SearchParamDTO> dto);
    @PostMapping("/feign/export/supplier")
    PagingVO<SupplierExportExcelDTO> exportSupplier(@RequestBody PagingDTO<SupplierDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/supplierReport")
    PagingVO<SupplierReportDTO.PagingViewDTO> exportSupplierReport(@RequestBody PagingDTO<SupplierReportDTO.ExportSearchParamDTO> dto);
    @PostMapping("/feign/export/supplierUser")
    PagingVO<SupplierUserVO> exportSupplierUser(@RequestBody PagingDTO<UserPagingSearchDTO> dto);
    @PostMapping("/feign/export/subcontractChangeOrder")
    PagingVO<SubcontractChangeDTO.ListDTO> exportSubcontractChangeOrder(@RequestBody PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/cfgSupplierSales")
    PagingVO<CfgSupplierSalesDTO.ListDTO> exportCfgSupplierSales(@RequestBody PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportSupplierCredential")
    PagingVO<SupplierCredentialDTO.ListDTO> exportSupplierCredential(@RequestBody PagingDTO<SupplierCredentialDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportSupplierVisit")
    PagingVO<SupplierVisitDTO.ListDTO> exportSupplierVisit(@RequestBody PagingDTO<SupplierVisitDTO.PagingParamDTO> dto);
    /**
     * 导出仓库绑定
     */
    @PostMapping("/feign/export/supplierRefWarehouse")
    PagingVO<SupplierRefWarehouseDTO.ListDTO> exportSupplierRefWarehouse(PagingDTO<SupplierRefWarehouseDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportContractInfo")
    PagingVO<ContractInfoDTO.ListDTO> exportContractInfo(@RequestBody PagingDTO<ContractInfoDTO.PagingParamDTO> dto);
    /**
     * 动态导出供应商
     */
    @PostMapping("/feign/export/exportDynamicSupplier")
    PagingVO<DynamicExcelDTO> exportDynamicSupplier(PagingDTO<SupplierDTO.PagingParamDTO> dto);
    /**
     * 全量导出供应商阶段审核
     */
    @PostMapping("/feign/export/exportSupplierPhase")
    PagingVO<SupplierPhaseExportExcelDTO> exportSupplierPhase(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto);

    /**
     * 动态导出供应商阶段审核
     */
    @PostMapping("/feign/export/exportDynamicSupplierPhase")
    PagingVO<DynamicExcelDTO> exportDynamicSupplierPhase(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto);
}
