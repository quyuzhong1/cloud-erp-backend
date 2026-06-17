package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_TRANSIT_REPORT;

@Component
@Slf4j
public class ExportWmsFbaTransitReportHandler extends AbstractPageFileEventHandler<FbaTransitCalculateReportDTO.ListDTO, FbaTransitCalculateReportDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/fbaTransitReportExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FBA_TRANSIT_REPORT;
    }


    @Override
    protected PagingVO<FbaTransitCalculateReportDTO.ListDTO> getPageData(PagingDTO<FbaTransitCalculateReportDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportFbaTransitReport(dto);
    }
}
