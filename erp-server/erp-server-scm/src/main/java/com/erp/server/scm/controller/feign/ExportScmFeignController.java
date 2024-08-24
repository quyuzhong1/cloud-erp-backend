package com.erp.server.scm.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
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
    @PostMapping("/purchaseApplication")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:paging",
            tableAlias = "pa")
    @WebAdvanceQuery(handler = PurchaseApplicationQueryHandler.class)
    public PagingVO<PurchaseApplicationDTO.ListDTO> exportPurchaseApplication(@RequestBody PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto) {
        return purchaseApplicationService.exportPurchaseApplication(dto);
    }

    @PostMapping("/purchaseChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "change_user_id",
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

}
