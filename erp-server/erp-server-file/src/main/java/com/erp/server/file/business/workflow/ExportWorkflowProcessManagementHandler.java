package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_MANAGEMENT;

@Component
@Slf4j
public class ExportWorkflowProcessManagementHandler extends AbstractPageFileEventHandler<ProcessManagementDTO.PagingResultDTO, ProcessManagementDTO.ExportDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;
    @Override
    public String getExcelPath() {
        return "excel/workflow/process_management.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_MANAGEMENT;
    }

    @Override
    protected List<ProcessManagementDTO.PagingResultDTO> getData(FileTask fileTask) {
        ProcessManagementDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProcessManagementDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProcessManagementDTO.PagingResultDTO> getPageData(PagingDTO<ProcessManagementDTO.ExportDTO> dto) {
        return exportWorkflowFeign.exportProcessManagement(dto);
    }
}
