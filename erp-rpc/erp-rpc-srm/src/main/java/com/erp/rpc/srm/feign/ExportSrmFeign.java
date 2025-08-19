package com.erp.rpc.srm.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.StatementDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.dto.SalesSharingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-srm", contextId = "exportSrmFeign", configuration = ExportFeignConfig.class)
public interface ExportSrmFeign {


    @PostMapping("/feign/export/poReconciliationDetail")
    PagingVO<PoReconciliationDetailDTO.ListDTO> exportPoReconciliationDetail(@RequestBody PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/poReconciliationDetailScm")
    PagingVO<PoReconciliationDetailDTO.ListDTO> exportPoReconciliationDetailScm(@RequestBody PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/poReconciliationScmExport")
    PagingVO<PoReconciliationDTO.ListDTO> exportPoReconciliationScmExport(@RequestBody PagingDTO<PoReconciliationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/poReconciliationExport")
    PagingVO<PoReconciliationDTO.ListDTO> exportPoReconciliationExport(@RequestBody PagingDTO<PoReconciliationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/poReconciliation")
    StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> exportPoReconciliation(@RequestBody PoReconciliationDTO.PagingParamDTO dto);
    @PostMapping("/feign/export/poReconciliationScm")
    StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> exportPoReconciliationScm(@RequestBody PoReconciliationDTO.PagingParamDTO dto);
    @PostMapping("/feign/export/salesSharing")
    PagingVO<SalesSharingDTO.ListDTO> exportSalesSharing(PagingDTO<SalesSharingDTO.PagingParamDTO> dto);
}
