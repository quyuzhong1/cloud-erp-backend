package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FIRST_MILE_PACKING_TASK_DETAIL;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PACKING_TASK_DETAIL;

@Component
@Slf4j
public class ExportWmsFirstMilePackingTaskDetailHandler extends AbstractPageFileEventHandler<WmsCartonDetailDTO.ListPackingDetailDTO, PackingTaskDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<WmsCartonDetailDTO.ListPackingDetailDTO> getData(FileTask fileTask) {
        PackingTaskDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PackingTaskDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> getPageData(PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        return exportWmsFeign.firstMilePackingTaskDetail(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FIRST_MILE_PACKING_TASK_DETAIL;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/firstMilePackingDetailExport.xlsx";
    }
}
