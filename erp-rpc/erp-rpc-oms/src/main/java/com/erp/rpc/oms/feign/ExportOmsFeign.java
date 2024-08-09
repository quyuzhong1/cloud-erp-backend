package com.erp.rpc.oms.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-oms", contextId = "exportOmsFeign")
public interface ExportOmsFeign {

    @PostMapping("/feign/export/customerB2BSellerChange")
    PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(@RequestBody PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto);
    @PostMapping("/feign/export/soChange")
    PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(@RequestBody PagingDTO<SoChangeDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/soReturn")
    PagingVO<SoReturnDTO.PagingView> exportSoReturn(@RequestBody PagingDTO<SoReturnDTO.PagingParam> dto);
    @PostMapping("/feign/export/soB2CAbnormal")
    PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(@RequestBody PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto);
}
