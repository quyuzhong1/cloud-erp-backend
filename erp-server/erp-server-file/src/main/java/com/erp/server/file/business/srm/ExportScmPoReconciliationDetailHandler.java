package com.erp.server.file.business.srm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.rpc.srm.feign.ExportSrmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PO_RECONCILIATION_DETAIL;

@Component
@Slf4j
public class ExportScmPoReconciliationDetailHandler extends AbstractPageFileEventHandler<PoReconciliationDTO.ExportDetailDTO, PoReconciliationDTO.PagingParamDTO> {
    @Resource
    private ExportSrmFeign exportSrmFeign;
    @Override
    protected List<PoReconciliationDTO.ExportDetailDTO> getData(FileTask fileTask) {
        PoReconciliationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoReconciliationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PoReconciliationDTO.ExportDetailDTO> getPageData(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        return exportSrmFeign.exportAllPoReconciliationDetail(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PO_RECONCILIATION_DETAIL;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseOrderExport.xlsx";
    }
}
