package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_TASK_TIME_RECORD;

@Component
@Slf4j
public class ExportPlmTaskTimeRecordHandler extends AbstractPageFileEventHandler<ProjectTaskTimeRecordPageVO, ProjectTaskTimeRecordDTO.PageRecordDto> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<ProjectTaskTimeRecordPageVO> getData(FileTask fileTask) {
        ProjectTaskTimeRecordDTO.PageRecordDto dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProjectTaskTimeRecordDTO.PageRecordDto>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProjectTaskTimeRecordPageVO> getPageData(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto) {
        return exportPlmFeign.exportTaskTimeRecord(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_TASK_TIME_RECORD;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/taskTime.xlsx";
    }
}
