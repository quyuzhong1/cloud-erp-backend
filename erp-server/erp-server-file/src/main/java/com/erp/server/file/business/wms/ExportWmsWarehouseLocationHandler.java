package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.vo.WarehouseLocationExportVo;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_LOCATION;


@Component
public class ExportWmsWarehouseLocationHandler extends AbstractPageFileEventHandler<WarehouseLocationExportVo, WarehouseLocationDTO.exportParamDto> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<WarehouseLocationExportVo> getData(FileTask fileTask) {
        WarehouseLocationDTO.exportParamDto dto = readValue(fileTask.getMetaInfo(), new TypeReference<WarehouseLocationDTO.exportParamDto>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<WarehouseLocationExportVo> getPageData(PagingDTO<WarehouseLocationDTO.exportParamDto> dto) {
        return exportWmsFeign.exportWarehouseLocation(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_WAREHOUSE_LOCATION;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/warehouseLocation.xlsx";
    }
}
