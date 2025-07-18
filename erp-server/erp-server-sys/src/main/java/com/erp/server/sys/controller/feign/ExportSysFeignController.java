package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.server.sys.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;
    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @PostMapping("/city")
    public PagingVO<DictCityDTO.PagingViewDTO> exportCity(@RequestBody PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        return dictCityService.exportCity(dto);
    }

    @PostMapping("/cityProvince")
    public PagingVO<DictCityDTO.PagingViewDTO> exportCityProvince(@RequestBody PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        return dictCityService.exportCityProvince(dto);
    }

    @PostMapping("/country")
    public PagingVO<DictCountryDTO.PagingViewDTO> exportCountry(@RequestBody PagingDTO<DictCountryDTO.PagingParamDTO> dto) {
        return dictCountryService.exportCountry(dto);
    }

    @PostMapping("/globalArea")
    public PagingVO<DictGlobalAreaDTO.PagingViewDTO> exportGlobalArea(@RequestBody PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto) {
        return dictGlobalAreaService.exportGlobalArea(dto);
    }
    @PostMapping("/exportCfgThirdNotice")
    public PagingVO<CfgThirdNoticeDTO.ListDTO> exportCfgThirdNotice(@RequestBody PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> dto){
        return cfgThirdNoticeService.paging(dto);
    }

    @PostMapping("/exportCfgThirdNoticePushRecord")
    public PagingVO<ThirdNoticePushRecordDTO.ListDTO> exportCfgThirdNoticePushRecord(@RequestBody PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> dto){
        return thirdNoticePushRecordService.paging(dto);
    }
}
