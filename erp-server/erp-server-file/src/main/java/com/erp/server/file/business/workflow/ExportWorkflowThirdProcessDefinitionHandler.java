package com.erp.server.file.business.workflow;


import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_THIRD_PROCESS_DEFINITION;

/**
 * 审批定义导出
 * @author will
 * @date 2025/7/1 17:24
 */
@Component
@Slf4j
public class ExportWorkflowThirdProcessDefinitionHandler extends AbstractPageFileEventHandler<ThirdProcessDefinitionDTO.ListDTO, ThirdProcessDefinitionDTO.PagingParamDTO> {
    @Resource
    private ExportWorkflowFeign exportWorkflowFeign;

    @Override
    protected PagingVO<ThirdProcessDefinitionDTO.ListDTO> getPageData(PagingDTO<ThirdProcessDefinitionDTO.PagingParamDTO> dto) {
        return exportWorkflowFeign.exportThirdProcessDefinition(dto);
    }

    @Override
    protected List<ThirdProcessDefinitionDTO.ListDTO> getData(FileTask fileTask) {
        ThirdProcessDefinitionDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ThirdProcessDefinitionDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/workflow/thirdProcessDefinition.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_THIRD_PROCESS_DEFINITION;
    }
}
