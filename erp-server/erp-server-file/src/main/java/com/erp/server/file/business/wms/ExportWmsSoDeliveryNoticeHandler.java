package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_DELIVERY_NOTICE;

@Component
@Slf4j
public class ExportWmsSoDeliveryNoticeHandler extends AbstractPageFileEventHandler<SoDeliveryNoticeDTO.PagingView, SoDeliveryNoticeDTO.PagingParam> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/soDeliveryNoticeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_DELIVERY_NOTICE;
    }

    @Override
    protected List<SoDeliveryNoticeDTO.PagingView> getData(FileTask fileTask) {
        SoDeliveryNoticeDTO.PagingParam dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoDeliveryNoticeDTO.PagingParam>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<SoDeliveryNoticeDTO.PagingView> getPageData(PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {
        return exportWmsFeign.exportSoDeliveryNotice(dto);
    }
}
