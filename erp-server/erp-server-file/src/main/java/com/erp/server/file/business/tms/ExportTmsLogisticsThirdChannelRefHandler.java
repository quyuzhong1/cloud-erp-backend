package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_THIRD_CHANNEL_REF;

@Component
@Slf4j
public class ExportTmsLogisticsThirdChannelRefHandler extends AbstractPageFileEventHandler<LogisticsThirdChannelRefDTO.PagingVO, LogisticsThirdChannelRefDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/logisticsThirdChannelRefExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_THIRD_CHANNEL_REF;
    }

    @Override
    protected List<LogisticsThirdChannelRefDTO.PagingVO> getData(FileTask fileTask) {
        LogisticsThirdChannelRefDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LogisticsThirdChannelRefDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsThirdChannelRefDTO.PagingVO> getPageData(PagingDTO<LogisticsThirdChannelRefDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportLogisticsThirdChannelRef(dto);
    }
}
