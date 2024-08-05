package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.enums.inventory.InventorySearchDimensionEnum;
import com.erp.rpc.wms.feign.WmsExportFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.erp.server.file.enums.FileTaskEventEnum.INVENTORY_EXPORT;

@Component
@Slf4j
public class InventoryExportHandler extends AbstractPageFileEventHandler<InventoryDTO.PagingViewDTO, InventoryDTO.ExportSearchParamDTO> {

    @Resource
    private WmsExportFeign wmsExportFeign;
    private static final ThreadLocal<InventoryDTO.ExportSearchParamDTO> threadLocal = new ThreadLocal<>();

    @Override
    protected List<InventoryDTO.PagingViewDTO> getData(FileTask fileTask) {
        InventoryDTO.ExportSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryDTO.ExportSearchParamDTO>() {
        });
        threadLocal.set(dto);
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<InventoryDTO.PagingViewDTO> getPageData(PagingDTO<InventoryDTO.ExportSearchParamDTO> dto) {
        return wmsExportFeign.getInventoryPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return INVENTORY_EXPORT;
    }

    @Override
    public String getExcelPath() {
        InventoryDTO.ExportSearchParamDTO param = threadLocal.get();
        String excelPath = "";
        if (param.getDimension().equals(InventorySearchDimensionEnum.WAREHOUSE.getCode())) {
            excelPath = "excel/wms/inventory.xlsx";
        }
        if (param.getDimension().equals(InventorySearchDimensionEnum.WAREHOUSE_AREA.getCode())) {
            excelPath = "excel/wms/inventory_area.xlsx";
        }
        if (param.getDimension().equals(InventorySearchDimensionEnum.WAREHOUSE_LOCATION.getCode())) {
            excelPath = "excel/wms/inventory_location.xlsx";
        }
        threadLocal.remove();
        return excelPath;
    }
}
