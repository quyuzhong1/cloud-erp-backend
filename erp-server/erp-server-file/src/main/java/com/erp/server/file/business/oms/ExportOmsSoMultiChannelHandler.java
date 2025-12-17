package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SO_MULTI_CHANNEL;

@Component
@Slf4j
public class ExportOmsSoMultiChannelHandler extends AbstractPageFileEventHandler<SoMultiChannelDTO.ListDTO, SoMultiChannelDTO.PagingParamDTO> {
    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoMultiChannelDTO.ListDTO> getData(FileTask fileTask) {
        SoMultiChannelDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoMultiChannelDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoMultiChannelDTO.ListDTO> getPageData(PagingDTO<SoMultiChannelDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportSoMultiChannel(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SO_MULTI_CHANNEL;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/soMultiChannel.xlsx";
    }
}
