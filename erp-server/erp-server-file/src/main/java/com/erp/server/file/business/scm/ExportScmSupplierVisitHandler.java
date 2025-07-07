package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_VISIT_REPORT;

@Component
@Slf4j
public class ExportScmSupplierVisitHandler extends AbstractPageFileEventHandler<SupplierVisitDTO.ListDTO, SupplierVisitDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierVisitExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_VISIT_REPORT;
    }

    @Override
    protected List<SupplierVisitDTO.ListDTO> getData(FileTask fileTask) {
        SupplierVisitDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierVisitDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SupplierVisitDTO.ListDTO> getPageData(PagingDTO<SupplierVisitDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportSupplierVisit(dto);
    }
}
