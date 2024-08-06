package com.erp.rpc.plm.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-plm",contextId = "ExportPlmFeign")
public interface ExportPlmFeign {

    @PostMapping("/feign/export/exportBom")
    PagingVO<BomExportExcelVO> exportBom(@RequestBody PagingDTO<SearchPagingDTO> dto);
}
