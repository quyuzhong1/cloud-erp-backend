package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_SALES;

@Component
@Slf4j
public class ExportWmsReportOrderSalesHandler extends AbstractPageFileEventHandler<ReportOrderSalesDTO.ListDTO, ReportOrderSalesDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected PagingVO<ReportOrderSalesDTO.ListDTO> getPageData(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> dto) {
        return exportWmsFeign.listReportOrderSales(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_REPORT_ORDER_SALES;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/reportOrderSales.xlsx";
    }
}
