package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_RECEIVE;


@Component
public class ExportWmsWarehouseReceiveHandler extends AbstractPageFileEventHandler<WarehouseReceiveExportExcelDTO, WarehouseReceiveDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<WarehouseReceiveExportExcelDTO> getPageData(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportWarehouseReceive(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_WAREHOUSE_RECEIVE;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/warehouseReceive.xlsx";
    }
}
