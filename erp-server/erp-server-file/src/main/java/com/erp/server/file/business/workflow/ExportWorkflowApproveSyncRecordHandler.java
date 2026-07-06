package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_APPROVE_SYNC_RECORD;

@Component
@Slf4j
public class ExportWorkflowApproveSyncRecordHandler extends AbstractPageFileEventHandler<ApproveSyncRecordDTO.ListDTO, ApproveSyncRecordDTO.PagingParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    public String getExcelPath() {
        return "excel/workflow/approveSyncRecordExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_APPROVE_SYNC_RECORD;
    }


    @Override
    protected PagingVO<ApproveSyncRecordDTO.ListDTO> getPageData(PagingDTO<ApproveSyncRecordDTO.PagingParamDTO> dto) {
        return exportWorkflowFeign.exportApproveSyncRecord(dto);
    }
}
