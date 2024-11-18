package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PICKING_LISTS;

@Component
@Slf4j
public class ExportWmsPickingListsHandler extends AbstractPageFileEventHandler<PickingListsDTO.ExportInfoDTO, PickingListsDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/pickingLists.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PICKING_LISTS;
    }

    @Override
    protected List<PickingListsDTO.ExportInfoDTO> getData(FileTask fileTask) {
        PickingListsDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PickingListsDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<PickingListsDTO.ExportInfoDTO> getPageData(PagingDTO<PickingListsDTO.ExportDTO> dto) {
        return exportWmsFeign.exportPickingLists(dto);
    }
}
