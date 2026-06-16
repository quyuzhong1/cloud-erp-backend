package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_OUTPUT_TASK;

/**
 * 推送任务 - 导出处理器
 */
@Component
public class ExportDmpOutputTaskHandler extends AbstractPageFileEventHandler<DmpOutputTaskDTO.ListDTO, DmpOutputTaskDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpOutputTaskDTO.ListDTO> getPageData(PagingDTO<DmpOutputTaskDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpOutputTask(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpOutputTask.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_OUTPUT_TASK;
    }
}
