package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_CFG_APPROVE_SYNC;

@Component
@Slf4j
public class ExportWorkflowCfgApproveSyncHandler extends AbstractPageFileEventHandler<CfgApproveSyncDTO.ListDTO, CfgApproveSyncDTO.PagingParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    public String getExcelPath() {
        return "excel/workflow/cfgApproveSyncExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_CFG_APPROVE_SYNC;
    }

    @Override
    protected List<CfgApproveSyncDTO.ListDTO> getData(FileTask fileTask) {
        CfgApproveSyncDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgApproveSyncDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CfgApproveSyncDTO.ListDTO> getPageData(PagingDTO<CfgApproveSyncDTO.PagingParamDTO> dto) {
        return exportWorkflowFeign.exportCfgApproveSync(dto);
    }
}
