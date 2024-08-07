package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_TASK;

@Component
@Slf4j
public class ExportPlmTaskHandler  extends AbstractPageFileEventHandler<TaskDTO.TaskExportDTO, TaskPagingDTO.ExportDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<TaskDTO.TaskExportDTO> getData(FileTask fileTask) {
        TaskPagingDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TaskPagingDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TaskDTO.TaskExportDTO> getPageData(PagingDTO<TaskPagingDTO.ExportDTO> dto) {
        return exportPlmFeign.exportTask(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_TASK;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/productTaskInfo.xlsx";
    }

}

