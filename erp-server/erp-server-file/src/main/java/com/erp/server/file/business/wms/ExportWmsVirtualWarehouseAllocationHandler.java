package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_WAREHOUSE_ALLOCATION;


@Component
@Slf4j
public class ExportWmsVirtualWarehouseAllocationHandler extends AbstractPageFileEventHandler<VirtualWarehouseAllocationDTO.ListDTO, VirtualWarehouseAllocationDTO.ExportDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    

    @Override
    protected List<VirtualWarehouseAllocationDTO.ListDTO> getData(FileTask fileTask) {
        VirtualWarehouseAllocationDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualWarehouseAllocationDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_WAREHOUSE_ALLOCATION;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/VirtualWarehouseAllocation.xlsx";
    }

    @Override
    protected PagingVO<VirtualWarehouseAllocationDTO.ListDTO> getPageData(PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto) {
        return exportWmsFeign.exportVirtualWarehouseAllocation(dto);
    }
}
