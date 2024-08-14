package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PACKING_TASK;

@Component
@Slf4j
public class ExportWmsPackingTaskHandler extends AbstractPageFileEventHandler<PackingTaskDTO.PagingViewDTO, PackingTaskDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<PackingTaskDTO.PagingViewDTO> getData(FileTask fileTask) {
        PackingTaskDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PackingTaskDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PackingTaskDTO.PagingViewDTO> getPageData(PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportPackingTask(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PACKING_TASK;
    }

    @Override
    public String getExcelPath() {

        return "excel/wms/packingTaskExport.xlsx";
    }
}
