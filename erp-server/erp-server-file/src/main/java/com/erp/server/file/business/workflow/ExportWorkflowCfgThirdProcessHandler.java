package com.erp.server.file.business.workflow;


import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.dto.CfgThirdProcessDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_THIRD_PROCESS;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-26
 *@Description:
 *@Version: 1.0
 */
@Component
@Slf4j
public class ExportWorkflowCfgThirdProcessHandler extends AbstractPageFileEventHandler<CfgThirdProcessDTO.ListDTO, CfgThirdProcessDTO.PagingParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    protected PagingVO<CfgThirdProcessDTO.ListDTO> getPageData(PagingDTO<CfgThirdProcessDTO.PagingParamDTO> dto) {
        return exportWorkflowFeign.exportCfgThirdProcess(dto);
    }

    @Override
    protected List<CfgThirdProcessDTO.ListDTO> getData(FileTask fileTask) {
        CfgThirdProcessDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgThirdProcessDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/workflow/CfgThirdProcess.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_THIRD_PROCESS;
    }
}
