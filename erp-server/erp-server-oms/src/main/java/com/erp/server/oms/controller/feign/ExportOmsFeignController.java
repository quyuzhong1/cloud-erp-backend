package com.erp.server.oms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
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
    private RefundOrderService refundOrderService;

    @Resource
    private SoB2cReturnService soB2cReturnService;
    @PostMapping("/customerB2BSellerChange")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    public PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(@RequestBody PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        return customerB2bSellerChangeService.exportCustomerB2BSellerChange(dto);
    }

    @PostMapping("/soChange")
    @WebAdvanceQuery(handler = SoChangeQueryHandler.class)
    public PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(@RequestBody PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        return soChangeService.exportSoChange(dto);
    }

    @PostMapping("/soReturn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SoReturnQueryHandler.class)
    public PagingVO<SoReturnDTO.PagingView> exportSoReturn(@RequestBody PagingDTO<SoReturnDTO.PagingParam> dto) {
        return soReturnService.exportSoReturn(dto);
    }

    @PostMapping("/soB2CAbnormal")
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    public PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        return soB2cService.exportSoB2CAbnormal(dto);
    }

    @PostMapping("/soB2C")
    @WebAdvanceQuery(handler = SoB2cQueryHandler.class)
    public PagingVO<SoB2cDTO.ExcelExportDTO> exportSoB2C(@RequestBody PagingDTO<SoB2cDTO.ExportParamDTO> dto) {
        return soB2cService.exportSoB2C(dto);
    }

    @PostMapping("/soB2CDeclare")
    public PagingVO<SoB2cDeclareProductDTO.ViewDTO> exportSoB2CDeclare(@RequestBody PagingDTO<SoB2cDeclareProductDTO.ListDTO> dto) {
        return soB2cDeclareProductService.exportSoB2CDeclare(dto);
    }

    @PostMapping("/soB2CProductSales")
    public PagingVO<ReportDTO.ProductSalesPagingViewDTO> exportSoB2CProductSales(@RequestBody PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        return soB2cService.exportSoB2CProductSales(dto);
    }

    @PostMapping("/platformSku")
    public PagingVO<SkuMappingDTO.PagingViewDTO> exportPlatformSku(@RequestBody PagingDTO<SkuMappingDTO.ExportDTO> dto) {
        return skuMappingService.exportPlatformSku(dto);
    }

    @PostMapping("/warehouseSku")
    public PagingVO<SkuMappingDTO.WarehousePagingViewDTO> exportWarehouseSku(@RequestBody PagingDTO<SkuMappingDTO.ExportWarehouseSkuDTO> dto) {
        return skuMappingService.exportWarehouseSku(dto);
    }

    @PostMapping("/shop")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:shop:export",
            serviceClass = ShopInfoService.class,
            keyIdName = "id")
    @WebAdvanceQuery(handler = ShopQueryHandler.class)
    public PagingVO<ShopDTO.PagingViewDTO> exportShop(@RequestBody PagingDTO<ShopDTO.ExportDTO> dto) {
        return shopInfoService.exportShop(dto);
    }

    @PostMapping("/customer")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    public PagingVO<CustomerDTO.PagingViewDTO> exportCustomer(@RequestBody PagingDTO<CustomerDTO.ExportDTO> dto) {
        return customerInfoService.exportCustomer(dto);
    }

    @PostMapping("/so")
    @WebAdvanceQuery(handler = SoInfoQueryHandler.class)
    public PagingVO<SoInfoDTO.PagingViewDTO> exportSo(@RequestBody PagingDTO<SoInfoDTO.ExportDTO> dto) {
        return soInfoService.exportSo(dto);
    }

    @PostMapping("/exportRefund")
    @WebAdvanceQuery
    public PagingVO<RefundOrderDTO.PagingViewDTO> exportRefund(@RequestBody PagingDTO<RefundOrderDTO.PagingParamDTO> dto) {
        return refundOrderService.exportRefund(dto);
    }

    @PostMapping("/exportSoB2cReturn")
    @WebAdvanceQuery
    public PagingVO<SoB2cReturnDTO.PagingViewDTO> exportSoB2cReturn(@RequestBody PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        return soB2cReturnService.paging(dto);
    }
}
