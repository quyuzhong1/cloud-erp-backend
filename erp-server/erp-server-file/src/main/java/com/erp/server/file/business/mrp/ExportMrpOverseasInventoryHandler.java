package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_OVERSEAS_INVENTORY;

@Component
public class ExportMrpOverseasInventoryHandler extends AbstractPageFileEventHandler<OverseasHistoryInventoryDTO.ListDTO, OverseasHistoryInventoryDTO.ExportDTO> {
    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected List<OverseasHistoryInventoryDTO.ListDTO> getData(FileTask fileTask) {
        OverseasHistoryInventoryDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<OverseasHistoryInventoryDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<OverseasHistoryInventoryDTO.ListDTO> getPageData(PagingDTO<OverseasHistoryInventoryDTO.ExportDTO> dto) {
        return exportMrpFeign.exportOverseasInventory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_OVERSEAS_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/mrp/overseasHisInventory.xlsx";
    }
}
