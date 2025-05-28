package com.erp.server.file.business.workflow;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_CFG_PROCESS;

/**
 * @description: 流程配置导出Handler
 * @author: hcg
 * @date: 2025/5/13 14:53
 */
@Component
@Slf4j
public class ExportWorkflowCfgProcessHandler extends AbstractPageFileEventHandler<CfgProcessDTO.ProcessViewDTO, CfgProcessDTO.SearchParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    protected List<CfgProcessDTO.ProcessViewDTO> getData(FileTask fileTask) {
        CfgProcessDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgProcessDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CfgProcessDTO.ProcessViewDTO> getPageData(PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        return exportWorkflowFeign.exportCfgProcess(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PROCESS_CFG_PROCESS;
    }

    @Override
    protected String getExcelPath() {
        return "excel/workflow/CFGProcess.xlsx";
    }

}
