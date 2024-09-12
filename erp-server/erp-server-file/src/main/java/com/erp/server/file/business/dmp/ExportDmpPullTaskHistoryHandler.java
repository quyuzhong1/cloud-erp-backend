package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PULL_TASK_HISTORY;

@Component
public class ExportDmpPullTaskHistoryHandler extends AbstractPageFileEventHandler<DmpPullTaskDTO.ListDTO, DmpPullTaskDTO.ParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected List<DmpPullTaskDTO.ListDTO> getData(FileTask fileTask) {
        DmpPullTaskDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpPullTaskDTO.ParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpPullTask.xlsx";
    }

    @Override
    protected PagingVO<DmpPullTaskDTO.ListDTO> getPageData(PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        return exportDmpFeign.exportPullTaskHistory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PULL_TASK_HISTORY;
    }
}
