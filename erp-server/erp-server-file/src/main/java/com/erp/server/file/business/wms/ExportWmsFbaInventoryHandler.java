package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_INVENTORY;

@Component
@Slf4j
public class ExportWmsFbaInventoryHandler extends AbstractPageFileEventHandler<FbaInventoryDTO.ListDTO, FbaInventoryDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<FbaInventoryDTO.ListDTO> getData(FileTask fileTask) {
        FbaInventoryDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FbaInventoryDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<FbaInventoryDTO.ListDTO> getPageData(PagingDTO<FbaInventoryDTO.ExportDTO> dto) {
        return exportWmsFeign.exportFbaInventory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FBA_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/fbaInventory.xlsx";
    }
}
