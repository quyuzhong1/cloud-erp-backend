package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY_TRANSPORT;

@Component
@Slf4j
public class ExportWmsInventoryTransportHandler extends AbstractPageFileEventHandler<InventoryReportDTO.TransportPagingDTO, InventoryReportDTO.ExportTransportSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<InventoryReportDTO.TransportPagingDTO> getData(FileTask fileTask) {
        InventoryReportDTO.ExportTransportSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryReportDTO.ExportTransportSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<InventoryReportDTO.TransportPagingDTO> getPageData(PagingDTO<InventoryReportDTO.ExportTransportSearchParamDTO> dto) {
        return exportWmsFeign.exportInventoryTransport(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INVENTORY_TRANSPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/transportInventory.xlsx";
    }
}
