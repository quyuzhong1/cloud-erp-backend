package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
    protected PagingVO<TmsAsyncTaskRecordDTO.ListDTO> getPageData(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportTmsAsyncTaskRecord(dto);
    }
}
