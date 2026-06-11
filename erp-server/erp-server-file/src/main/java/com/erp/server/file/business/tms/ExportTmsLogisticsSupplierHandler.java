package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_SUPPLIER;

@Component
@Slf4j
public class ExportTmsLogisticsSupplierHandler extends AbstractPageFileEventHandler<LogisticsSupplierDTO.PagingViewDTO, LogisticsSupplierDTO.ExportDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/logisticsSupplier.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_SUPPLIER;
    }


    @Override
    protected PagingVO<LogisticsSupplierDTO.PagingViewDTO> getPageData(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto) {
        return exportTmsFeign.exportLogisticsSupplier(dto);
    }
}
