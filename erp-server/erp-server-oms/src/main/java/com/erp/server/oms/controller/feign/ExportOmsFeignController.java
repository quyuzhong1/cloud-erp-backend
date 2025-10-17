package com.erp.server.oms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.model.oms.dto.excel.SoPriceChangeExportExcelDTO;
import com.erp.model.oms.dto.excel.SoPriceExportExcelDTO;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.erp.server.oms.query.*;
import com.erp.server.oms.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportOmsFeignController {

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;
    @Resource
    private SoChangeService soChangeService;
    @Resource
    private SoReturnService soReturnService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private SoB2cDeclareProductService soB2cDeclareProductService;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private SoInfoService soInfoService;

    @Resource
    private ListingPushRecordService listingPushRecordService;
    @Resource
    private SoB2cRefundService soB2cRefundService;

    @Resource
    private SoB2cReturnService soB2cReturnService;
    @Resource
    private ReportManagerService reportManagerService;
    @Resource
    private InvoiceInfoService invoiceInfoService;
    @Resource
    private FullyManagedOrderService fullyManagedOrderService;

    @Resource
    private SoPriceService soPriceService;
    @Resource
    private SoPriceChangeService soPriceChangeService;
    @Resource
    private SoMultiChannelService soMultiChannelService;

    @Resource
    private ExhibitionOrderService exhibitionOrderService;
    @Resource
    private PackagePlanService packagePlanService;


    @Resource
    private CfgInvoiceInvalidService cfgInvoiceInvalidService;

    @Resource
    private SoReceiptService soReceiptService;

    @PostMapping("/customerB2BSellerChange")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    public PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(@RequestBody PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        return customerB2bSellerChangeService.exportCustomerB2BSellerChange(dto);
    }

    @PostMapping("/soChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "si.warehouse_id",
            menuCode = "oms:soChange:paging",
            tableAlias = "sc"
    )
    @WebAdvanceQuery(handler = SoChangeQueryHandler.class)
    public PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(@RequestBody PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        return soChangeService.exportSoChange(dto);
    }

    @PostMapping("/soReturn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sr.warehouse_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SoReturnQueryHandler.class)
    public PagingVO<SoReturnDTO.PagingView> exportSoReturn(@RequestBody PagingDTO<SoReturnDTO.PagingParam> dto) {
        return soReturnService.exportSoReturn(dto);
    }

    @PostMapping("/soB2CAbnormal")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sb2cd.warehouse_id",
            shopTableField = "sb2c.shop_id",
            menuCode = "oms:soB2c:paging"
    )
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    public PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        return soB2cService.exportSoB2CAbnormal(dto);
    }
    @PostMapping("/soB2CAbnormalPools")
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    public PagingVO<SoB2cAbnormalDTO.PoolsDTO> exportSoB2CAbnormalPools(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        return soB2cErrorService.exportSoB2CAbnormalPools(dto);
    }

    @PostMapping("/soB2C")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sb2cd.warehouse_id",
            shopTableField = "sb2c.shop_id",
            menuCode = "oms:soB2c:paging"
    )
    @WebAdvanceQuery(handler = SoB2cQueryHandler.class)
    public PagingVO<SoB2cDTO.ExcelExportDTO> exportSoB2C(@RequestBody PagingDTO<SoB2cDTO.ExportParamDTO> dto) {
        return soB2cService.exportSoB2C(dto);
    }
    @PostMapping("/exportFullyManagedOrder")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sb2cd.warehouse_id",
            shopTableField = "sb2c.shop_id",
            menuCode = "oms:fully:paging"
    )
    @WebAdvanceQuery(handler = FullyManagedQueryHandler.class)
    public PagingVO<SoB2cDTO.ExcelExportDTO> exportFullyManagedOrder(@RequestBody PagingDTO<SoB2cDTO.ExportParamDTO> dto) {
        return fullyManagedOrderService.exportFullyManagedOrder(dto);
    }

    @PostMapping("/soB2CDeclare")
    public PagingVO<SoB2cDeclareProductDTO.ViewDTO> exportSoB2CDeclare(@RequestBody PagingDTO<SoB2cDeclareProductDTO.ListDTO> dto) {
        return soB2cDeclareProductService.exportSoB2CDeclare(dto);
    }

    @PostMapping("/soB2CProductSales")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "sb.shop_id",
            menuCode = "oms:reportManager:productSalesPaging"
    )
    @WebAdvanceQuery
    public PagingVO<ReportDTO.ProductSalesPagingViewDTO> exportSoB2CProductSales(@RequestBody PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        return reportManagerService.exportSoB2CProductSales(dto);
    }

    @PostMapping("/platformSku")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "sm.shop_id",
            menuCode = "oms:skuMaping:platformPaging"
    )
    @WebAdvanceQuery
    public PagingVO<SkuMappingDTO.PagingViewDTO> exportPlatformSku(@RequestBody PagingDTO<SkuMappingDTO.ExportDTO> dto) {
        return skuMappingService.exportPlatformSku(dto);
    }

    /**
     * b2b平台sku对照表信息导出
     * @author will
     * @date 2025/8/27 16:37
     * @param dto
     * @return PagingVO<PagingViewDTO>
     */
    @PostMapping("/b2bPlatformSku")
    @WebAdvanceQuery
    public PagingVO<SkuMappingDTO.PagingViewDTO> exportB2bPlatformSku(@RequestBody PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        return skuMappingService.b2bPlatformPaging(dto);
    }

    @PostMapping("/warehouseSku")
    @WebAdvanceQuery
    public PagingVO<SkuMappingDTO.WarehousePagingViewDTO> exportWarehouseSku(@RequestBody PagingDTO<SkuMappingDTO.ExportWarehouseSkuDTO> dto) {
        return skuMappingService.exportWarehouseSku(dto);
    }

    @PostMapping("/exportListingPush")
    @WebAdvanceQuery
    public PagingVO<ListingPushRecordDTO.PagingViewDTO> exportListingPush(@RequestBody PagingDTO<ListingPushRecordDTO.PagingParamDTO> dto) {
        return listingPushRecordService.paging(dto);
    }

    @PostMapping("/customerSku")
    @WebAdvanceQuery
    public PagingVO<SkuMappingDTO.CustomerPagingViewDTO> exportCustomerSku(@RequestBody PagingDTO<SkuMappingDTO.CustomerPagingParamDTO> dto) {
        dto.getParams().setExport(true);
        return skuMappingService.customerPaging(dto);
    }
    @PostMapping("/shop")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "si.id",
            menuCode = "oms:shop:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = ShopQueryHandler.class)
    public PagingVO<ShopDTO.PagingViewDTO> exportShop(@RequestBody PagingDTO<ShopDTO.ExportDTO> dto) {
        return shopInfoService.exportShop(dto);
    }

    @PostMapping("/customer")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    public PagingVO<CustomerDTO.PagingExportDTO> exportCustomer(@RequestBody PagingDTO<CustomerDTO.ExportDTO> dto) {
        return customerInfoService.exportCustomer(dto);
    }

    @PostMapping("/so")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            warehouseTableField = "si.warehouse_id",
            menuCode = "oms:so:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = SoInfoQueryHandler.class)
    public PagingVO<SoInfoDTO.PagingViewDTO> exportSo(@RequestBody PagingDTO<SoInfoDTO.ExportDTO> dto) {
        return soInfoService.exportSo(dto);
    }

    @PostMapping("/exportRefund")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "ro.shop_id",
            menuCode = "oms:refundOrder:paging"
    )
    @WebAdvanceQuery
    public PagingVO<SoB2cRefundDTO.PagingViewDTO> exportRefund(@RequestBody PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto) {
        return soB2cRefundService.exportRefund(dto);
    }

    @PostMapping("/exportSoB2cReturn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "sbr.shop_id",
            menuCode = "oms:soB2cReturn:paging"
    )
    @WebAdvanceQuery(handler = SoB2cReturnQueryHandler.class)
    public PagingVO<SoB2cReturnDTO.PagingViewDTO> exportSoB2cReturn(@RequestBody PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        return soB2cReturnService.paging(dto);
    }

    @PostMapping("/exportInvoice")
    @WebAdvanceQuery
    public PagingVO<InvoiceInfoDTO.PagingViewDTO> exportInvoice(@RequestBody PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto) {
        return invoiceInfoService.paging(dto, true);
    }

    @PostMapping("/exportInvoiceInvalid")
    @WebAdvanceQuery
    public PagingVO<CfgInvoiceInvalidDTO.PagingViewDTO> exportInvoiceInvalid(@RequestBody PagingDTO<CfgInvoiceInvalidDTO.PagingParamDTO> dto) {
        return cfgInvoiceInvalidService.paging(dto);
    }
    @PostMapping("/exportSoReceipt")
    @WebAdvanceQuery
    public PagingVO<SoReceiptDTO.ListDTO> exportSoReceipt(@RequestBody PagingDTO<SoReceiptDTO.PagingParamDTO> dto) {
        return soReceiptService.paging(dto);
    }
    /**
     * 销售价目表导出
     * @param dto
     * @return PurchasePriceExportExcelDTO
     */
    @PostMapping("/soPrice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "oms:so:price:paging",
            tableAlias = "sp")
    @WebAdvanceQuery(handler = SoPriceQueryHandler.class)
    public PagingVO<SoPriceExportExcelDTO> exportSoPrice(@RequestBody PagingDTO<SoPriceDTO.PagingParamDTO> dto) {
        return soPriceService.exportSoPrice(dto);
    }

    /**
     * 销售调价表导出
     * @param dto
     * @return PurchasePriceExportExcelDTO
     */
    @PostMapping("/soPriceChange")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "oms:soPriceChange:paging",
            tableAlias = "sp")
    @WebAdvanceQuery(handler = SoPriceChangeQueryHandler.class)
    public PagingVO<SoPriceChangeExportExcelDTO> exportSoPriceChange(@RequestBody PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto) {
        return soPriceChangeService.exportSoPriceChange(dto);
    }


    @PostMapping("/soMultiChannel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "smc.delivery_shop_id",
            menuCode = "oms:soMultiChannel:paging",
            tableAlias = "smc"
    )
    @WebAdvanceQuery
    public PagingVO<SoMultiChannelDTO.ListDTO> exportSoMultiChannel(@RequestBody PagingDTO<SoMultiChannelDTO.PagingParamDTO> dto) {
        return soMultiChannelService.paging(dto);
    }
    @PostMapping("/packagePlan")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:packagePlan:paging",
            tableAlias = "pp"
    )
    @WebAdvanceQuery
    public PagingVO<PackagePlanDTO.ExportDTO> exportPackagePlan(@RequestBody PagingDTO<PackagePlanDTO.PagingParamDTO> dto) {
        return packagePlanService.exportPaging(dto);
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-08-21
     * @param dto
     * @return
     */
    @PostMapping("/exportExhibitionOrder")
    @WebAdvanceQuery(handler = ExhibitionOrderQueryHandler.class)
    public PagingVO<ExhibitionOrderDTO.ListDTO> exportExhibitionOrder(@RequestBody PagingDTO<ExhibitionOrderDTO.PagingParamDTO> dto) {
        return exhibitionOrderService.paging(dto);
    }

}
