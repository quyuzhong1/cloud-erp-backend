package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierReportDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.rpc.wms.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_REPORT;

@Component
@Slf4j
public class ExportScmSupplierReportHandler extends AbstractPageFileEventHandler<SupplierReportDTO.PagingViewDTO, SupplierReportDTO.ExportSearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<SupplierReportDTO.PagingViewDTO> getData(FileTask fileTask) {
        SupplierReportDTO.ExportSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierReportDTO.ExportSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SupplierReportDTO.PagingViewDTO> getPageData(PagingDTO<SupplierReportDTO.ExportSearchParamDTO> dto) {
        return exportScmFeign.exportSupplierReport(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_REPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierRpt.xlsx";
    }
}
