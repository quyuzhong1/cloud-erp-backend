package com.erp.server.file.business.srm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.rpc.srm.feign.ExportSrmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_PO_RECONCILIATION_SCM_EXPORT;

@Component
public class ExportSrmPoReconciliationScmExportHandler extends AbstractPageFileEventHandler<PoReconciliationDTO.ListDTO, PoReconciliationDTO.PagingParamDTO> {

    @Resource
    private ExportSrmFeign exportSrmFeign;

    @Override
    protected List<PoReconciliationDTO.ListDTO> getData(FileTask fileTask) {
        PoReconciliationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoReconciliationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PoReconciliationDTO.ListDTO> getPageData(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        return exportSrmFeign.exportPoReconciliationScmExport(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/srm/poReconciliation.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SRM_PO_RECONCILIATION_SCM_EXPORT;
    }
}
