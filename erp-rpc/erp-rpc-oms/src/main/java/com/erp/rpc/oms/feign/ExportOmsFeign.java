package com.erp.rpc.oms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.model.oms.dto.excel.SoPriceChangeExportExcelDTO;
import com.erp.model.oms.dto.excel.SoPriceExportExcelDTO;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "exportOmsFeign", configuration = ExportFeignConfig.class)
public interface ExportOmsFeign {

    @PostMapping("/feign/export/customerB2BSellerChange")
    PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(@RequestBody PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto);
    @PostMapping("/feign/export/soChange")
    PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(@RequestBody PagingDTO<SoChangeDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/soReturn")
    PagingVO<SoReturnDTO.PagingView> exportSoReturn(@RequestBody PagingDTO<SoReturnDTO.PagingParam> dto);
    @PostMapping("/feign/export/soB2CAbnormal")
    PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/soB2CAbnormalPools")
    PagingVO<SoB2cAbnormalDTO.PoolsDTO> exportSoB2CAbnormalPools(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/soB2C")
    PagingVO<SoB2cDTO.ExcelExportDTO> exportSoB2C(Request.Options options, @RequestBody PagingDTO<SoB2cDTO.ExportParamDTO> dto);
    @PostMapping("/feign/export/exportFullyManagedOrder")
    PagingVO<SoB2cDTO.ExcelExportDTO> exportFullyManagedOrder(Request.Options options, @RequestBody PagingDTO<SoB2cDTO.ExportParamDTO> dto);
    @PostMapping("/feign/export/soB2CDeclare")
    PagingVO<SoB2cDeclareProductDTO.ViewDTO> exportSoB2CDeclare(@RequestBody PagingDTO<SoB2cDeclareProductDTO.ListDTO> dto);
    @PostMapping("/feign/export/soB2CProductSales")
    PagingVO<ReportDTO.ProductSalesPagingViewDTO> exportSoB2CProductSales(@RequestBody PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto);
    @PostMapping("/feign/export/platformSku")
    PagingVO<SkuMappingDTO.PagingViewDTO> exportPlatformSku(@RequestBody PagingDTO<SkuMappingDTO.ExportDTO> dto);
    @PostMapping("/feign/export/warehouseSku")
    PagingVO<SkuMappingDTO.WarehousePagingViewDTO> exportWarehouseSku(@RequestBody PagingDTO<SkuMappingDTO.ExportWarehouseSkuDTO> dto);
    @PostMapping("/feign/export/exportListingPush")
    PagingVO<ListingPushRecordDTO.PagingViewDTO> exportListingPush(@RequestBody PagingDTO<ListingPushRecordDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/customerSku")
    PagingVO<SkuMappingDTO.CustomerPagingViewDTO> exportCustomerSku(@RequestBody PagingDTO<SkuMappingDTO.CustomerPagingParamDTO> dto);
    @PostMapping("/feign/export/shop")
    PagingVO<ShopDTO.PagingViewDTO> exportShop(@RequestBody PagingDTO<ShopDTO.ExportDTO> dto);
    @PostMapping("/feign/export/customer")
    PagingVO<CustomerDTO.PagingExportDTO> exportCustomer(@RequestBody PagingDTO<CustomerDTO.ExportDTO> dto);
    @PostMapping("/feign/export/exportCustomerPairList")
    List<cn.hutool.core.lang.Pair<Integer, List<?>>> exportCustomerPairList(@RequestBody PagingDTO<CustomerDTO.ExportDTO> dto);
    @PostMapping("/feign/export/so")
    PagingVO<SoInfoDTO.PagingViewDTO> exportSo(@RequestBody PagingDTO<SoInfoDTO.ExportDTO> dto);
    @PostMapping("/feign/export/exportRefund")
    PagingVO<SoB2cRefundDTO.PagingViewDTO> exportRefund(@RequestBody PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportSoB2cReturn")
    PagingVO<SoB2cReturnDTO.PagingViewDTO> exportSoB2cReturn(@RequestBody PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportInvoice")
    PagingVO<InvoiceInfoDTO.PagingViewDTO> exportInvoice(@RequestBody PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto);

    //收款单导出
    @PostMapping("/feign/export/exportSoReceipt")
    PagingVO<SoReceiptDTO.ListDTO> exportSoReceipt(@RequestBody PagingDTO<SoReceiptDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/soPriceChange")
    PagingVO<SoPriceChangeExportExcelDTO> exportSoPriceChange(@RequestBody PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto);
    //销售价目导出
    @PostMapping("/feign/export/soPrice")
    PagingVO<SoPriceExportExcelDTO> exportSoPrice(@RequestBody PagingDTO<SoPriceDTO.PagingParamDTO> dto);
    /**
     * 导出展会订单
     */
    @PostMapping("/feign/export/exportExhibitionOrder")
    PagingVO<ExhibitionOrderDTO.ListDTO> exportExhibitionOrder(@RequestBody PagingDTO<ExhibitionOrderDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportInvoiceInvalid")
    PagingVO<CfgInvoiceInvalidDTO.PagingViewDTO> exportInvoiceInvalid(@RequestBody PagingDTO<CfgInvoiceInvalidDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/soMultiChannel")
    PagingVO<SoMultiChannelDTO.ListDTO> exportSoMultiChannel(@RequestBody PagingDTO<SoMultiChannelDTO.PagingParamDTO> dto);

    /**
     * b2b平台sku对照表信息导出
     * @author will
     * @date 2025/8/27 16:35
     * @param dto
     * @return PagingVO<PagingViewDTO>
     */
    @PostMapping("/feign/export/b2bPlatformSku")
    PagingVO<SkuMappingDTO.PagingViewDTO> exportB2bPlatformSku(PagingDTO<SkuMappingDTO.PagingParamDTO> dto);
}
