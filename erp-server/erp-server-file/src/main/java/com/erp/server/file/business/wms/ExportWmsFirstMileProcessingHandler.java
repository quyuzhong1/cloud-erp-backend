package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FIRST_MILE_PROCESSING;

@Component
@Slf4j
public class ExportWmsFirstMileProcessingHandler extends AbstractPageFileEventHandler<FirstMileProcessingDTO.ListDTO, FirstMileProcessingDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<FirstMileProcessingDTO.ListDTO> getData(FileTask fileTask) {
        FirstMileProcessingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FirstMileProcessingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<FirstMileProcessingDTO.ListDTO> getPageData(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportFirstMileProcessing(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FIRST_MILE_PROCESSING;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/firstMileProcessing.xlsx";
    }
}
