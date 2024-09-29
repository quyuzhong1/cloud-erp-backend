package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_DEFINITION;

@Component
@Slf4j
public class ExportWorkflowProcessDefinitionHandler extends AbstractPageFileEventHandler<ProcessDefinitionDTO.ExportDTO, ProcessDefinitionDTO.QueryExportDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    public String getExcelPath() {
        return "excel/workflow/processDefinition.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_DEFINITION;
    }

    @Override
    protected List<ProcessDefinitionDTO.ExportDTO> getData(FileTask fileTask) {
        ProcessDefinitionDTO.QueryExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProcessDefinitionDTO.QueryExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProcessDefinitionDTO.ExportDTO> getPageData(PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto) {
        return exportWorkflowFeign.exportProcessDefinition(dto);
    }
}
