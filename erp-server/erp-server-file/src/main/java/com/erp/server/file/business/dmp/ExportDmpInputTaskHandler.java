package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_INPUT_TASK;

/**
 * 拉取任务 - 导出处理器
 */
@Component
public class ExportDmpInputTaskHandler extends AbstractPageFileEventHandler<DmpInputTaskDTO.ListDTO, DmpInputTaskDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpInputTaskDTO.ListDTO> getPageData(PagingDTO<DmpInputTaskDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpInputTask(dto);
    }

    @Override
    protected List<DmpInputTaskDTO.ListDTO> getData(FileTask fileTask) {
        DmpInputTaskDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpInputTaskDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpinputtask.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_INPUT_TASK;
    }
}
