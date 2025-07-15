package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_DELEGATE;

@Component
@Slf4j
public class ExportWorkflowProcessDelegateHandler extends AbstractPageFileEventHandler<ProcessDelegateDTO.ListDTO, ProcessDelegateDTO.PagingParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    public String getExcelPath() {
        return "excel/workflow/processDelegate.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_DELEGATE;
    }

    @Override
    protected List<ProcessDelegateDTO.ListDTO> getData(FileTask fileTask) {
        ProcessDelegateDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProcessDelegateDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProcessDelegateDTO.ListDTO> getPageData(PagingDTO<ProcessDelegateDTO.PagingParamDTO> dto) {
        return exportWorkflowFeign.exportProcessDelegate(dto);
    }
}
