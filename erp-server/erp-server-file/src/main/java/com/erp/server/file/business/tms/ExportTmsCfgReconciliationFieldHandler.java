package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_CFG_RECONCILIATION_FIELD;

@Component
@Slf4j
public class ExportTmsCfgReconciliationFieldHandler extends AbstractPageFileEventHandler<CfgReconciliationFieldExportExcelDTO, CfgReconciliationFieldDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/CfgReconciliationField.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_CFG_RECONCILIATION_FIELD;
    }

    @Override
    protected List<CfgReconciliationFieldExportExcelDTO> getData(FileTask fileTask) {
        CfgReconciliationFieldDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgReconciliationFieldDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CfgReconciliationFieldExportExcelDTO> getPageData(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportCfgReconciliationField(dto);
    }
}
