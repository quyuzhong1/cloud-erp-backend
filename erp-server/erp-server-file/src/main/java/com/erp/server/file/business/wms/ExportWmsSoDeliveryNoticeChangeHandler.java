package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    protected List<SoDeliveryNoticeChangeDTO.ListDTO> getData(FileTask fileTask) {
        SoDeliveryNoticeChangeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoDeliveryNoticeChangeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> getPageData(PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSoDeliveryNoticeChange(dto);
    }
}
