package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2C_PROCESSING;

@Component
@Slf4j
public class ExportWmsSoB2cProcessingHandler extends AbstractPageFileEventHandler<SoB2cProcessingDTO.ListDTO, SoB2cProcessingDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<SoB2cProcessingDTO.ListDTO> getData(FileTask fileTask) {
        SoB2cProcessingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2cProcessingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2cProcessingDTO.ListDTO> getPageData(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSoB2cProcessing(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_B2C_PROCESSING;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/soB2cProcessing.xlsx";
    }
}
