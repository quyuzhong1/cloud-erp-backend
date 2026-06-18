package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_RETURN_NOTICE;

@Component
@Slf4j
public class ExportWmsSoReturnNoticeHandler extends AbstractPageFileEventHandler<SoReturnNoticeDTO.PagingView, SoReturnNoticeDTO.PagingParam> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/soReturnNoticeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_RETURN_NOTICE;
    }



    @Override
    protected PagingVO<SoReturnNoticeDTO.PagingView> getPageData(PagingDTO<SoReturnNoticeDTO.PagingParam> dto) {
        return exportWmsFeign.exportSoReturnNotice(dto);
    }
}
