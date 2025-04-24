package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_B2C_DECLARE_RECONCILIATION;

@Component
@Slf4j
public class ExportTmsB2cDeclareReconciliationHandler extends AbstractPageFileEventHandler<TmsB2cDeclareReconciliationDTO.ListDTO, TmsB2cDeclareReconciliationDTO.ExportDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/tmsB2cDeclareReconciliation.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_TMS_B2C_DECLARE_RECONCILIATION;
    }

    @Override
    protected List<TmsB2cDeclareReconciliationDTO.ListDTO> getData(FileTask fileTask) {
        TmsB2cDeclareReconciliationDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TmsB2cDeclareReconciliationDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> getPageData(PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto) {
        return exportTmsFeign.exportB2cDeclareReconciliation(dto);
    }
}
