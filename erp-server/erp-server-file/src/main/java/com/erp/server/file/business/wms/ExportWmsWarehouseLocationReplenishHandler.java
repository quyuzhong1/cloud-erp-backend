package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_LOCATION_REPLENISH;


@Component
public class ExportWmsWarehouseLocationReplenishHandler extends AbstractPageFileEventHandler<WarehouseLocationReplenishDTO.ViewDTO, WarehouseLocationReplenishDTO.ExportParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<WarehouseLocationReplenishDTO.ViewDTO> getData(FileTask fileTask) {
        WarehouseLocationReplenishDTO.ExportParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<WarehouseLocationReplenishDTO.ExportParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<WarehouseLocationReplenishDTO.ViewDTO> getPageData(PagingDTO<WarehouseLocationReplenishDTO.ExportParamDTO> dto) {
        return exportWmsFeign.exportWarehouseLocationReplenish(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_WAREHOUSE_LOCATION_REPLENISH;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/warehouseLocationReplenishExport.xlsx";
    }
}
