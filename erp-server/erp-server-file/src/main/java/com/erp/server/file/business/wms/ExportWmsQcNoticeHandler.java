package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_NOTICE_REPORT;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportWmsQcNoticeHandler extends AbstractPageFileEventHandler<QcNoticeDTO.ListDTO, QcNoticeDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<QcNoticeDTO.ListDTO> getPageData(PagingDTO<QcNoticeDTO.ExportDTO> dto) {
        return exportWmsFeign.exportQcNotice(dto);
    }

    @Override
    protected List<QcNoticeDTO.ListDTO> getData(FileTask fileTask) {
        QcNoticeDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcNoticeDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/qcNoticeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_NOTICE_REPORT;
    }
}
