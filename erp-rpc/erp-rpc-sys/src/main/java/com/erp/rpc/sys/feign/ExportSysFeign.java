package com.erp.rpc.sys.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-sys", contextId = "exportSysFeign", configuration = ExportFeignConfig.class)
public interface ExportSysFeign {

    @PostMapping("/feign/export/city")
    PagingVO<DictCityDTO.PagingViewDTO> exportCity(@RequestBody PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);
    @PostMapping("/feign/export/cityProvince")
    PagingVO<DictCityDTO.PagingViewDTO> exportCityProvince(@RequestBody PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);
    @PostMapping("/feign/export/country")
    PagingVO<DictCountryDTO.PagingViewDTO> exportCountry(@RequestBody PagingDTO<DictCountryDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/globalArea")
    PagingVO<DictGlobalAreaDTO.PagingViewDTO> exportGlobalArea(@RequestBody PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportCfgThirdNotice")
    PagingVO<CfgThirdNoticeDTO.ListDTO> exportCfgThirdNotice(@RequestBody PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportCfgThirdNoticePushRecord")
    PagingVO<ThirdNoticePushRecordDTO.ListDTO> exportCfgThirdNoticePushRecord(@RequestBody PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportTemplateManagement")
    PagingVO<TemplateManagementDTO.ListDTO> exportTemplateManagement(@RequestBody PagingDTO<TemplateManagementDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportDictBasicAll")
    PagingVO<DictBasicAllDTO.ViewDTO> exportDictBasicAll(@RequestBody PagingDTO<DictBasicAllDTO.PagingParamDTO> dto);
}
