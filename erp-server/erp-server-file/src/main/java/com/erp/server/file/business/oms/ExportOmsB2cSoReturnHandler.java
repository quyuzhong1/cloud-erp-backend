package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_RETURN;

@Component
@Slf4j
public class ExportOmsB2cSoReturnHandler extends AbstractPageFileEventHandler<SoB2cReturnDTO.PagingViewDTO, SoB2cReturnDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoB2cReturnDTO.PagingViewDTO> getData(FileTask fileTask) {
        SoB2cReturnDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2cReturnDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2cReturnDTO.PagingViewDTO> getPageData(PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportSoB2cReturn(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_B2C_RETURN;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/SoB2cReturnOrder.xlsx";
    }
}
