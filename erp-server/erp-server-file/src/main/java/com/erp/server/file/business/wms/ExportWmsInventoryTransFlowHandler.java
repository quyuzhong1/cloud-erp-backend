package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY_TRANS_FLOW;

@Component
@Slf4j
public class ExportWmsInventoryTransFlowHandler extends AbstractPageFileEventHandler<InventoryDTO.TransFlowPagingViewDTO, InventoryDTO.ExportInvFlowSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<InventoryDTO.TransFlowPagingViewDTO> getData(FileTask fileTask) {
        InventoryDTO.ExportInvFlowSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryDTO.ExportInvFlowSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<InventoryDTO.TransFlowPagingViewDTO> getPageData(PagingDTO<InventoryDTO.ExportInvFlowSearchParamDTO> dto) {
        return exportWmsFeign.exportInventoryTransFlow(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INVENTORY_TRANS_FLOW;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/inventoryFlow.xlsx";
    }
}
