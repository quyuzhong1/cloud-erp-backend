package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.server.plm.service.BomInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export/")
public class ExportPlmFeignController {
    @Resource
    private BomInfoService bomInfoService;
    @PostMapping("/exportBom")
    PagingVO<BomExportExcelVO> exportBom(@RequestBody PagingDTO<SearchPagingDTO> dto) {
       return bomInfoService.exportBom(dto);
    }
}
