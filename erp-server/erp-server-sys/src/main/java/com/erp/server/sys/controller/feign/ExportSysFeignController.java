package com.erp.server.sys.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.server.sys.query.TemplateManagementQueryHandler;
import com.erp.server.sys.service.*;
import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.server.sys.query.DictBasicAllQueryHandler;
import com.erp.server.sys.service.CfgThirdNoticeService;
import com.erp.server.sys.service.DictBasicAllService;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import com.erp.server.sys.service.ThirdNoticePushRecordService;

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
    @Resource
    private TemplateManagementService templateManagementService;
    @Resource
    private DictBasicAllService dictBasicAllService;

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

    @PostMapping("/exportTemplateManagement")
    @WebAdvanceQuery(handler = TemplateManagementQueryHandler.class)
    public PagingVO<TemplateManagementDTO.ListDTO> exportTemplateManagement(@RequestBody  PagingDTO<TemplateManagementDTO.PagingParamDTO> dto){
        return templateManagementService.paging(dto);
    }
    
    @PostMapping("/exportDictBasicAll")
    @WebAdvanceQuery(handler = DictBasicAllQueryHandler.class)
    public PagingVO<DictBasicAllDTO.ViewDTO> exportDictBasicAll(@RequestBody PagingDTO<DictBasicAllDTO.PagingParamDTO> dto){
    	return dictBasicAllService.paging(dto);
    }
}
