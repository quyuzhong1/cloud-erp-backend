package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PUSH_TASK_HISTORY;

@Component
public class ExportDmpPushTaskHistoryHandler extends AbstractPageFileEventHandler<DmpPushTaskDTO.ListDTO, DmpPushTaskDTO.ParamDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected List<DmpPushTaskDTO.ListDTO> getData(FileTask fileTask) {
        DmpPushTaskDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpPushTaskDTO.ParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpPushTask.xlsx";
    }

    @Override
    protected PagingVO<DmpPushTaskDTO.ListDTO> getPageData(PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        return exportDmpFeign.exportPushTaskHistory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PUSH_TASK_HISTORY;
    }
}
