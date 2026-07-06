package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_DEMAND;

@Component
@Slf4j
public class ExportWmsReportOrderDemandHandler extends AbstractPageFileEventHandler<ReportOrderDemandDTO.ListDTO, ReportOrderDemandDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


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
