package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.enums.inventory.InventorySearchDimensionEnum;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY_AGE;

@Component
@Slf4j
public class ExportWmsInventoryAgeHandler extends AbstractPageFileEventHandler<InventoryDTO.PagingViewDTO, InventoryReportDTO.ExportInventoryAgeSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<InventoryDTO.PagingViewDTO> getData(FileTask fileTask) {
        InventoryReportDTO.ExportInventoryAgeSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryReportDTO.ExportInventoryAgeSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<InventoryDTO.PagingViewDTO> getPageData(PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto) {
        return exportWmsFeign.exportInventoryAge(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INVENTORY_AGE;
    }

    @Override
    public String getExcelPath() {

        return "";
    }
}
