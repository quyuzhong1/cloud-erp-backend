package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REMOTE_POSTCODE;

@Component
@Slf4j
public class ExportTmsRemotePostcodeHandler extends AbstractPageFileEventHandler<RemotePostcodeDTO.ExportListDTO, RemotePostcodeDTO.ExportDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/remotePostcodeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_REMOTE_POSTCODE;
    }


    @Override
    protected PagingVO<RemotePostcodeDTO.ExportListDTO> getPageData(PagingDTO<RemotePostcodeDTO.ExportDTO> dto) {
        return exportTmsFeign.exportRemotePostcode(dto);
    }
}
