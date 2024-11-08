package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_VIRTUAL_INVENTORY;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_INVENTORY;

@Component
public class ExportMrpVirtualInventoryHandler extends AbstractPageFileEventHandler<FbaHistoryInventoryDTO.ListDTO, FbaHistoryInventoryDTO.ExportDTO> {
    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected List<FbaHistoryInventoryDTO.ListDTO> getData(FileTask fileTask) {
        FbaHistoryInventoryDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FbaHistoryInventoryDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<FbaHistoryInventoryDTO.ListDTO> getPageData(PagingDTO<FbaHistoryInventoryDTO.ExportDTO> dto) {
        return exportMrpFeign.exportFbaInventory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_VIRTUAL_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/virtualInventory.xlsx";
    }
}
