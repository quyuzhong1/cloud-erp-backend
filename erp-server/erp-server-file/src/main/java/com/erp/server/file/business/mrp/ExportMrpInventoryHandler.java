package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_LOCAL_INVENTORY;

@Component
public class ExportMrpInventoryHandler extends AbstractPageFileEventHandler<LocalHistoryInventoryDTO.PagingViewDTO, LocalHistoryInventoryDTO.ExportDTO> {
    @Resource
    private ExportMrpFeign exportMrpFeign;


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
