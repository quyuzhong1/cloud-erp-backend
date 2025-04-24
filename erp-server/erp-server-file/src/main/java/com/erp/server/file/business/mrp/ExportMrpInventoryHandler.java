package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_LOCAL_INVENTORY;

@Component
public class ExportMrpInventoryHandler extends AbstractPageFileEventHandler<LocalHistoryInventoryDTO.PagingViewDTO, LocalHistoryInventoryDTO.ExportDTO> {
    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected List<LocalHistoryInventoryDTO.PagingViewDTO> getData(FileTask fileTask) {
        LocalHistoryInventoryDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LocalHistoryInventoryDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LocalHistoryInventoryDTO.PagingViewDTO> getPageData(PagingDTO<LocalHistoryInventoryDTO.ExportDTO> dto) {
        return exportMrpFeign.exportLocalInventory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_LOCAL_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/mrp/localInventory.xlsx";
    }
}
