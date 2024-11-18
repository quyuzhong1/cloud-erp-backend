package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    protected List<LogisticsSupplierDTO.PagingViewDTO> getData(FileTask fileTask) {
        LogisticsSupplierDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LogisticsSupplierDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsSupplierDTO.PagingViewDTO> getPageData(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto) {
        return exportTmsFeign.exportLogisticsSupplier(dto);
    }
}
