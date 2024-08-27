package com.erp.server.file.business.wms;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY_AGE;

@Component
@Slf4j
public class ExportWmsInventoryAgeHandler extends AbstractDynamicHeadersFileEventHandler<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        InventoryReportDTO.ExportInventoryAgeSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryReportDTO.ExportInventoryAgeSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto) {
        return exportWmsFeign.exportWmsInventoryAge(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INVENTORY_AGE;
    }
}
