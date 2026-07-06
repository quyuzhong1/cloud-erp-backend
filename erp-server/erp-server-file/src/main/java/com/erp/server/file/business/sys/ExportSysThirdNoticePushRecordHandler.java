package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE_RECORD;

@Component
@Slf4j
public class ExportSysThirdNoticePushRecordHandler extends AbstractPageFileEventHandler<ThirdNoticePushRecordDTO.ListDTO, ThirdNoticePushRecordDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;

    @Override
    public String getExcelPath() {
        return "excel/sys/cfgThirdNoticePushRecordExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_THIRD_NOTICE_RECORD;
    }


    @Override
    protected PagingVO<ThirdNoticePushRecordDTO.ListDTO> getPageData(PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportCfgThirdNoticePushRecord(dto);
    }
}
