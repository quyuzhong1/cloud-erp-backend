package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_DAILY_QC_BILL;

@Component
@Slf4j
public class ExportWmsDailyQcBillHandler extends AbstractPageFileEventHandler<QcInfoDTO.QcDailyReportDTO, QcInfoDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<QcInfoDTO.QcDailyReportDTO> getData(FileTask fileTask) {
        QcInfoDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcInfoDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<QcInfoDTO.QcDailyReportDTO> getPageData(PagingDTO<QcInfoDTO.ExportDTO> dto) {
        return exportWmsFeign.exportDailyQcBill(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_DAILY_QC_BILL;
    }

    @Override
    public String getExcelPath() {
        // todo
        return
                "";
    }
}
