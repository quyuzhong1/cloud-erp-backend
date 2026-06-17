package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_CREDENTIAL_REPORT;

@Component
@Slf4j
public class ExportScmSupplierCredentialHandler extends AbstractPageFileEventHandler<SupplierCredentialDTO.ListDTO, SupplierCredentialDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierCredentialExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_CREDENTIAL_REPORT;
    }


    @Override
    protected PagingVO<SupplierCredentialDTO.ListDTO> getPageData(PagingDTO<SupplierCredentialDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportSupplierCredential(dto);
    }
}
