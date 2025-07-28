package com.erp.server.scm.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.*;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.server.scm.query.*;
import com.erp.server.scm.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportScmFeignController {
    @Resource
    private PurchaseChangeService purchaseChangeService;
    @Resource
    private PurchaseApplicationService purchaseApplicationService;
    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;
    @Resource
    private PurchasePriceService purchasePriceService;
    @Resource
    private SalesDemandService salesDemandService;
    @Resource
    private SubcontractChangeService subcontractChangeService;
    @Resource
    private SupplierService supplierService;
    @Resource
    private SupplierReportService supplierReportService;
    @Resource
    private SupplierUserService supplierUserService;
    @Resource
    private CfgSupplierSalesService cfgSupplierSalesService;
    @Resource
    private SupplierCredentialService supplierCredentialService;
    @Resource
    private SupplierVisitService supplierVisitService;
    @Resource
    private SupplierRefWarehouseService supplierRefWarehouseService;
    @Resource
    private ContractInfoService contractInfoService;
    @Resource
    private SupplierPhaseService supplierPhaseService;

    @PostMapping("/purchaseApplication")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id,create_user_id",
            warehouseTableField = "pad.dest_warehouse_id",
            menuCode = "scm:purchaseApplication:paging",
            tableAlias = "pa")
    @WebAdvanceQuery(handler = PurchaseApplicationQueryHandler.class)
    public PagingVO<PurchaseApplicationDTO.ListDTO> exportPurchaseApplication(@RequestBody PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto) {
        return purchaseApplicationService.exportPurchaseApplication(dto);
    }

    @PostMapping("/purchaseChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "change_user_id",
            warehouseTableField = "pc.delivery_warehouse_id",
            menuCode = "scm:purchaseChange:exportExcel",
            tableAlias = "pc")
    @WebAdvanceQuery
    public PagingVO<PurchaseChangeExportExcelDTO> exportPurchaseChange(@RequestBody PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        return purchaseChangeService.exportPurchaseChange(dto);
    }

    @PostMapping("/purchaseOrderContract")
    public PagingVO<BomExportExcelVO> exportPurchaseOrderContract(@RequestBody PagingDTO<String> dto) {
        return purchaseOrderService.exportPurchaseOrderContract(dto);
    }

    @PostMapping("/purchaseOrder")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            warehouseTableField = "po.delivery_warehouse_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    @WebAdvanceQuery(handler = PurchaseOrderQueryHandler.class)
    public PagingVO<PurchaseOrderDTO.ListDTO> exportPurchaseOrder(@RequestBody PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        return purchaseOrderService.exportPurchaseOrder(dto);
    }

    @PostMapping("/purchasePriceChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:change:paging",
            tableAlias = "pp")
    @WebAdvanceQuery(handler = PurchasePriceChangeQueryHandler.class)
    public PagingVO<PurchasePriceChangeExportExcelDTO> exportPurchasePriceChange(@RequestBody PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        return purchasePriceChangeService.exportPurchasePriceChange(dto);
    }

    @PostMapping("/purchasePrice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:paging",
            tableAlias = "pp")
    @WebAdvanceQuery(handler = PurchasePriceQueryHandler.class)
    public PagingVO<PurchasePriceExportExcelDTO> exportPurchasePrice(@RequestBody PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        return purchasePriceService.exportPurchasePrice(dto);
    }

    @PostMapping("/salesDemand")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            warehouseTableField = "sdd.dest_warehouse_id",
            shopTableField = "sd.shop_id",
            menuCode = "scm:salesDemand:paging",
            tableAlias = "sd")
    @WebAdvanceQuery
    public PagingVO<SalesDemandExportExcelDTO> exportSalesDemand(@RequestBody PagingDTO<SalesDemandDTO.SearchParamDTO> dto) {
        return salesDemandService.exportSalesDemand(dto);
    }

    @PostMapping("/supplier")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:paging",
            tableAlias = "supplier"
    )
    @WebAdvanceQuery(handler = SupplierQueryHandler.class)
    public PagingVO<SupplierExportExcelDTO> exportSupplier(@RequestBody PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        return supplierService.exportSupplier(dto);
    }

    @PostMapping("/supplierReport")
    public PagingVO<SupplierReportDTO.PagingViewDTO> exportSupplierReport(@RequestBody PagingDTO<SupplierReportDTO.ExportSearchParamDTO> dto) {
        return supplierReportService.exportSupplierReport(dto);
    }

    @PostMapping("/supplierUser")
    @WebAdvanceQuery(handler = SupplierUserQueryHandler.class)
    public PagingVO<SupplierUserVO> exportSupplierUser(@RequestBody PagingDTO<UserPagingSearchDTO> dto) {
        return supplierUserService.exportSupplierUser(dto);
    }

    @PostMapping("/subcontractChangeOrder")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:export",
            tableAlias = "sc"
    )
    @WebAdvanceQuery(handler = SubcontractChangeQueryHandler.class)
    public PagingVO<SubcontractChangeDTO.ListDTO> exportSubcontractChangeOrder(@RequestBody PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto) {
        return subcontractChangeService.exportSubcontractChangeOrder(dto);
    }

    @PostMapping("/cfgSupplierSales")
    @WebAdvanceQuery
    public PagingVO<CfgSupplierSalesDTO.ListDTO> exportCfgSupplierSales(@RequestBody PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> dto) {
        return cfgSupplierSalesService.paging(dto);
    }

    @PostMapping("/exportSupplierCredential")
    @WebAdvanceQuery(handler = SupplierCredentialQueryHandler.class)
    public PagingVO<SupplierCredentialDTO.ListDTO> exportSupplierCredential(@RequestBody PagingDTO<SupplierCredentialDTO.PagingParamDTO> dto) {
        return supplierCredentialService.paging(dto);
    }

    @PostMapping("/exportSupplierVisit")
    @WebAdvanceQuery(handler = SupplierVisitQueryHandler.class)
    public PagingVO<SupplierVisitDTO.ListDTO> exportSupplierVisit(@RequestBody PagingDTO<SupplierVisitDTO.PagingParamDTO> dto) {
        return supplierVisitService.pagingList(dto);
    }


    /**
     * 导出数据查询
     * @author will
     * @date 2025/6/19 11:03
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/supplierRefWarehouse")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplierRefWarehouse:paging",
            tableAlias = "sc"
    )
    @WebAdvanceQuery
    public PagingVO<SupplierRefWarehouseDTO.ListDTO> exportSupplierRefWarehouse(@RequestBody PagingDTO<SupplierRefWarehouseDTO.PagingParamDTO> dto) {
        return supplierRefWarehouseService.paging(dto);
    }

    @PostMapping("/exportContractInfo")
    @WebAdvanceQuery(handler = ContractInfoQueryHandler.class)
    public PagingVO<ContractInfoDTO.ListDTO> exportContractInfo(@RequestBody PagingDTO<ContractInfoDTO.PagingParamDTO> dto){
        return contractInfoService.paging(dto);
    }

    /**
     * 导出动态供应商
     * @author will
     * @date 2025/7/28 11:34
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    @PostMapping("/exportDynamicSupplier")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:paging",
            tableAlias = "supplier"
    )
    @WebAdvanceQuery(handler = SupplierQueryHandler.class)
    public PagingVO<DynamicExcelDTO> exportDynamicSupplier(@RequestBody PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        return supplierService.exportDynamicSupplier(dto);
    }

    /**
     * 导出供应商阶段
     * @author will
     * @date 2025/7/28 11:10
     * @param dto
     * @return PagingVO<SupplierPhaseExportExcelDTO>
     */
    @PostMapping("/exportSupplierPhase")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    @WebAdvanceQuery(handler = SupplierPhaseQueryHandler.class)
    public PagingVO<SupplierPhaseExportExcelDTO> exportSupplierPhase(@RequestBody PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        return supplierPhaseService.exportSupplierPhase(dto);
    }

    /**
     * 动态导出供应商阶段
     * @author will
     * @date 2025/7/28 11:10
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    @PostMapping("/exportDynamicSupplierPhase")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    @WebAdvanceQuery(handler = SupplierPhaseQueryHandler.class)
    public PagingVO<DynamicExcelDTO> exportDynamicSupplierPhase(@RequestBody PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        return supplierPhaseService.exportDynamicSupplierPhase(dto);
    }
}
