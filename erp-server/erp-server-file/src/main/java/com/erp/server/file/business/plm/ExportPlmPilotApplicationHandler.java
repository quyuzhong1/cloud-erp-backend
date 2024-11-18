package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PILOT_APPLICATION;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportPlmPilotApplicationHandler extends AbstractPageFileEventHandler<PilotApplicationDTO.ListDTO, PilotApplicationDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<PilotApplicationDTO.ListDTO> getPageData(PagingDTO<PilotApplicationDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportPilotApplication(dto);
    }

    @Override
    protected List<PilotApplicationDTO.ListDTO> getData(FileTask fileTask) {
        PilotApplicationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PilotApplicationDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/pilotApplicationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PILOT_APPLICATION;
    }
}
