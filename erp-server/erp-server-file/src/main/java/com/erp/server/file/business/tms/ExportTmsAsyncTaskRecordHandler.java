package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_ASYNC_TASK_RECORD;


@Component
@Slf4j
public class ExportTmsAsyncTaskRecordHandler extends AbstractPageFileEventHandler<TmsAsyncTaskRecordDTO.ListDTO, TmsAsyncTaskRecordDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/tmsAsyncTaskRecord.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_ASYNC_TASK_RECORD;
    }

    @Override
    protected List<TmsAsyncTaskRecordDTO.ListDTO> getData(FileTask fileTask) {
        TmsAsyncTaskRecordDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TmsAsyncTaskRecordDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TmsAsyncTaskRecordDTO.ListDTO> getPageData(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportTmsAsyncTaskRecord(dto);
    }
}
