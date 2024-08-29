package com.erp.server.file.business.wms;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.dto.excel.ExportQcReportExcelDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_REPORT_DETAIL;

@Component
@Slf4j
public class ExportWmsQcReportDetailHandler extends AbstractPageFileEventHandler<ExportQcReportExcelDTO, BaseIdDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<ExportQcReportExcelDTO> getData(FileTask fileTask) {
        BaseIdDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseIdDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ExportQcReportExcelDTO> getPageData(PagingDTO<BaseIdDTO> dto) {
        return exportWmsFeign.exportQcReportDetail(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_REPORT_DETAIL;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/qcDetail.xlsx";
    }
}
