package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.excel.SupplierExportExcelDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER;

@Component
@Slf4j
public class ExportScmSupplierHandler extends AbstractPageFileEventHandler<SupplierExportExcelDTO, SupplierDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<SupplierExportExcelDTO> getData(FileTask fileTask) {
        SupplierDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SupplierExportExcelDTO> getPageData(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportSupplier(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierExport.xlsx";
    }
}
