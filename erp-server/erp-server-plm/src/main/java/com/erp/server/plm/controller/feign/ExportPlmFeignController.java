package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductCertificateService;
import com.erp.server.plm.service.TaskService;
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
    @Resource
    private ProductCertificateService productCertificateService;
    @Resource
    private TaskService taskService;
    @PostMapping("/exportBom")
    public PagingVO<BomExportExcelVO> exportBom(@RequestBody PagingDTO<SearchPagingDTO> dto) {
       return bomInfoService.exportBom(dto);
    }

    @PostMapping("/exportProductCertificate")
    public PagingVO<ProductCertificateDTO.ListDTO> exportProductCertificate(@RequestBody PagingDTO<ProductCertificateDTO.ExportParamDTO> dto) {
        return productCertificateService.exportProductCertificate(dto);
    }

    @PostMapping("/exportTask")
    PagingVO<TaskDTO.TaskExportDTO> exportTask(@RequestBody PagingDTO<TaskPagingDTO.ExportDTO> dto){
        return taskService.exportTask(dto);
    }
}
