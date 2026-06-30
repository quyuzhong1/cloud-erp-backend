package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_RETURN_RECEIVE;

@Component
@Slf4j
public class ExportWmsSoReturnReceiveHandler extends AbstractPageFileEventHandler<SoReturnReceiveDTO.PagingView, SoReturnReceiveDTO.PagingParam> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/soReturnReceiveExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_RETURN_RECEIVE;
    }



    @Override
    protected PagingVO<SoReturnReceiveDTO.PagingView> getPageData(PagingDTO<SoReturnReceiveDTO.PagingParam> dto) {
        return exportWmsFeign.exportSoReturnReceive(dto);
    }
}
