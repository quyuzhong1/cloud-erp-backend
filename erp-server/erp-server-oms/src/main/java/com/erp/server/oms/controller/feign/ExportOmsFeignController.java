package com.erp.server.oms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.server.oms.query.CustomerInfoQueryHandler;
import com.erp.server.oms.query.SoB2cAbnormalQueryHandler;
import com.erp.server.oms.query.SoChangeQueryHandler;
import com.erp.server.oms.query.SoReturnQueryHandler;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoChangeService;
import com.erp.server.oms.service.SoReturnService;
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

    @PostMapping("/customerB2BSellerChange")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(@RequestBody PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        return customerB2bSellerChangeService.exportCustomerB2BSellerChange(dto);
    }

    @PostMapping("/feign/export/soChange")
    @WebAdvanceQuery(handler = SoChangeQueryHandler.class)
    PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(@RequestBody PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        return soChangeService.exportSoChange(dto);
    }

    @PostMapping("/feign/export/soReturn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SoReturnQueryHandler.class)
    PagingVO<SoReturnDTO.PagingView> exportSoReturn(@RequestBody PagingDTO<SoReturnDTO.PagingParam> dto) {
        return soReturnService.exportSoReturn(dto);
    }

    @PostMapping("/feign/export/soB2CAbnormal")
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto){
        return soB2cService.exportSoB2CAbnormal(dto);
    }
}
