package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_B2C_TOTAL_PROCESSING_EXPORT;

@Component
@Slf4j
public class ExportWmsReportB2cProcessingHandler extends AbstractPageFileEventHandler<ReportProcessingDTO.ListDTO,ReportProcessingDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected List<ReportProcessingDTO.ListDTO> getData(FileTask fileTask) {
        ReportProcessingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReportProcessingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ReportProcessingDTO.ListDTO> getPageData(PagingDTO<ReportProcessingDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportTotalB2cProcessing(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_B2C_TOTAL_PROCESSING_EXPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/reportProcessing.xlsx";
    }
}
