package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.dto.excel.SupplierPhaseExportExcelDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_PHASE;

@Component
@Slf4j
public class ExportScmSupplierPhaseHandler extends AbstractPageFileEventHandler<SupplierPhaseExportExcelDTO, SupplierPhaseDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    protected PagingVO<SupplierPhaseExportExcelDTO> getPageData(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportSupplierPhase(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_PHASE;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierPhaseExport.xlsx";
    }
}
