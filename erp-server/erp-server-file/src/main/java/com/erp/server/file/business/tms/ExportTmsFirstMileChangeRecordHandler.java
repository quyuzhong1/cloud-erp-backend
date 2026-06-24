package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FIRST_MILE_CHANGE_RECORD;

@Component
@Slf4j
public class ExportTmsFirstMileChangeRecordHandler extends AbstractPageFileEventHandler<FirstMileChangeRecordDTO.PagingVO, FirstMileChangeRecordDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/firstMileChangeRecordExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_FIRST_MILE_CHANGE_RECORD;
    }


    @Override
    protected PagingVO<FirstMileChangeRecordDTO.PagingVO> getPageData(PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportFirstMileChangeRecord(dto);
    }
}
