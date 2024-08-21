package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportSysFeignController {
    @Resource
    private DictGlobalAreaService dictGlobalAreaService;
    @Resource
    private DictCountryService dictCountryService;
    @Resource
    private DictCityService dictCityService;

    @PostMapping("/city")
    PagingVO<DictCityDTO.PagingViewDTO> exportCity(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        return dictCityService.exportCity(dto);
    }

    @PostMapping("/cityProvince")
    PagingVO<DictCityDTO.PagingViewDTO> exportCityProvince(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        return dictCityService.exportCityProvince(dto);
    }

    @PostMapping("/country")
    PagingVO<DictCountryDTO.PagingViewDTO> exportCountry(PagingDTO<DictCountryDTO.PagingParamDTO> dto) {
        return dictCountryService.exportCountry(dto);
    }

    @PostMapping("/globalArea")
    PagingVO<DictGlobalAreaDTO.PagingViewDTO> exportGlobalArea(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto) {
        return dictGlobalAreaService.exportGlobalArea(dto);
    }
}
