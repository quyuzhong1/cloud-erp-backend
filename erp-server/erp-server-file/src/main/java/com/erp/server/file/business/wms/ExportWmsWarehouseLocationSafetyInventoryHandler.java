package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_LOCATION_SAFETY_INVENTORY;


@Component
public class ExportWmsWarehouseLocationSafetyInventoryHandler extends AbstractPageFileEventHandler<WarehouseLocationSafetyInventoryDTO.ViewDTO, WarehouseLocationSafetyInventoryDTO.exportParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<WarehouseLocationSafetyInventoryDTO.ViewDTO> getData(FileTask fileTask) {
        WarehouseLocationSafetyInventoryDTO.exportParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<WarehouseLocationSafetyInventoryDTO.exportParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> getPageData(PagingDTO<WarehouseLocationSafetyInventoryDTO.exportParamDTO> dto) {
        return exportWmsFeign.exportWarehouseLocationSafetyInventory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_WAREHOUSE_LOCATION_SAFETY_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/warehouseLocationSafetyInventoryExport.xlsx";
    }
}
