package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_DELIVERY_NOTICE_CHANGE;

@Component
@Slf4j
public class ExportWmsSoDeliveryNoticeChangeHandler extends AbstractPageFileEventHandler<SoDeliveryNoticeChangeDTO.ListDTO, SoDeliveryNoticeChangeDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/soDeliveryNoticeChangeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_DELIVERY_NOTICE_CHANGE;
    }



    @Override
    protected PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> getPageData(PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSoDeliveryNoticeChange(dto);
    }
}
