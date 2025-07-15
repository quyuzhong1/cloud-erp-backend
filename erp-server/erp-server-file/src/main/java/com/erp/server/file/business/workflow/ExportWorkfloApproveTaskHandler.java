package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_APPROVE_TASK;

@Component
@Slf4j
public class ExportWorkfloApproveTaskHandler extends AbstractPageFileEventHandler<ApproveTaskInfoDTO.ListDTO, ApproveTaskInfoDTO.PagingParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    public String getExcelPath() {
        return "excel/workflow/approveTaskInfo.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_APPROVE_TASK;
    }

    @Override
    protected List<ApproveTaskInfoDTO.ListDTO> getData(FileTask fileTask) {
        ApproveTaskInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ApproveTaskInfoDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ApproveTaskInfoDTO.ListDTO> getPageData(PagingDTO<ApproveTaskInfoDTO.PagingParamDTO> dto) {
        return exportWorkflowFeign.exportApproveTaskInfo(dto);
    }
}
