package com.erp.rpc.sys.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "erp-sys", contextId = "exportSysFeign")
public interface ExportSysFeign {

    @PostMapping("/feign/export/city")
    PagingVO<DictCityDTO.PagingViewDTO> exportCity(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);
    @PostMapping("/feign/export/cityProvince")
    PagingVO<DictCityDTO.PagingViewDTO> exportCityProvince(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);
    @PostMapping("/feign/export/country")
    PagingVO<DictCountryDTO.PagingViewDTO> exportCountry(PagingDTO<DictCountryDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/globalArea")
    PagingVO<DictGlobalAreaDTO.PagingViewDTO> exportGlobalArea(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto);
}
