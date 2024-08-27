package com.erp.server.file.business.srm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.rpc.srm.feign.ExportSrmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_PO_RECONCILIATION_DETAIL_SCM;

@Component
public class ExportSrmPoReconciliationDetailScmHandler extends AbstractPageFileEventHandler<PoReconciliationDetailDTO.ListDTO, PoReconciliationDetailDTO.PagingParamDTO> {
    @Resource
    private ExportSrmFeign exportSrmFeign;
    @Override
    protected List<PoReconciliationDetailDTO.ListDTO> getData(FileTask fileTask) {
        PoReconciliationDetailDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoReconciliationDetailDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/srm/poReconciliationDetail.xlsx";
    }

    @Override
    protected PagingVO<PoReconciliationDetailDTO.ListDTO> getPageData(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto) {
        return exportSrmFeign.exportPoReconciliationDetailScm(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SRM_PO_RECONCILIATION_DETAIL_SCM;
    }
}
