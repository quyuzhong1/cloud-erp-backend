package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_WAREHOUSE_REPORT;

@Component
@Slf4j
public class ExportWmsVirtualWarehouseReportHandler extends AbstractPageFileEventHandler<VirtualWarehouseDTO.ExportDTO, VirtualWarehouseDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/virtualWarehouseExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_WAREHOUSE_REPORT;
    }

    @Override
    protected List<VirtualWarehouseDTO.ExportDTO> getData(FileTask fileTask) {
        VirtualWarehouseDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualWarehouseDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<VirtualWarehouseDTO.ExportDTO> getPageData(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportVirtualWarehouse(dto);
    }
}
