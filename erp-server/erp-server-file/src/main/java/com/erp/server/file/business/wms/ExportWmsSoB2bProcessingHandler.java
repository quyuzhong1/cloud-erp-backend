package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2B_PROCESSING;

@Component
@Slf4j
public class ExportWmsSoB2bProcessingHandler extends AbstractPageFileEventHandler<SoB2bProcessingDTO.ListDTO, SoB2bProcessingDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<SoB2bProcessingDTO.ListDTO> getData(FileTask fileTask) {
        SoB2bProcessingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2bProcessingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2bProcessingDTO.ListDTO> getPageData(PagingDTO<SoB2bProcessingDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSoB2bProcessing(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_B2B_PROCESSING;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/soB2bProcessing.xlsx";
    }
}
