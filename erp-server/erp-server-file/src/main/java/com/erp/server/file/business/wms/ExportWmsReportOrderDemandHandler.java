package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_DEMAND;

@Component
@Slf4j
public class ExportWmsReportOrderDemandHandler extends AbstractPageFileEventHandler<ReportOrderDemandDTO.ListDTO, ReportOrderDemandDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<ReportOrderDemandDTO.ListDTO> getData(FileTask fileTask) {
        ReportOrderDemandDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReportOrderDemandDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ReportOrderDemandDTO.ListDTO> getPageData(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto) {
        return exportWmsFeign.listReportOrderDemand(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_REPORT_ORDER_DEMAND;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/reportOrderDemand.xlsx";
    }
}
