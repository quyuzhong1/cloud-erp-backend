package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_ADDRESS;

@Component
@Slf4j
public class ExportTmsLogisticsAddressHandler extends AbstractPageFileEventHandler<LogisticsAddressDTO.PagingViewDTO, LogisticsAddressDTO.ExportDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/tms/LogisticsAddress.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_ADDRESS;
    }

    @Override
    protected List<LogisticsAddressDTO.PagingViewDTO> getData(FileTask fileTask) {
        LogisticsAddressDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LogisticsAddressDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsAddressDTO.PagingViewDTO> getPageData(PagingDTO<LogisticsAddressDTO.ExportDTO> dto) {
        return exportTmsFeign.exportLogisticsAddress(dto);
    }
}
