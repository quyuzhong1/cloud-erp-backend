package com.erp.server.file.business.srm;

import com.common.business.dto.StatementDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.rpc.srm.feign.ExportSrmFeign;
import com.erp.server.file.core.AbstractDetailPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_PO_RECONCILIATION_SCM;

@Component
public class ExportSrmPoReconciliationScmHandler extends AbstractDetailPageFileEventHandler<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> {
    @Resource
    private ExportSrmFeign exportSrmFeign;

    @Override
    protected StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> getData(FileTask fileTask) {
        PoReconciliationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoReconciliationDTO.PagingParamDTO>() {
        });
        return exportSrmFeign.exportPoReconciliationScm(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/srm/exportPoReconciliation.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SRM_PO_RECONCILIATION_SCM;
    }
}
