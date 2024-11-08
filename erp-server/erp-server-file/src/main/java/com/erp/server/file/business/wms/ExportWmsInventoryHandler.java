package com.erp.server.file.business.wms;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.utils.CollectionUtils;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.enums.inventory.InventorySearchDimensionEnum;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY;

@Component
@Slf4j
public class ExportWmsInventoryHandler extends AbstractPageFileEventHandler<InventoryDTO.PagingViewDTO, InventoryDTO.ExportSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
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
        PagingVO<InventoryDTO.PagingViewDTO> inventoryPageData = exportWmsFeign.getInventoryPageData(dto);
        List<InventoryDTO.PagingViewDTO> dataList = (List<InventoryDTO.PagingViewDTO>) inventoryPageData.getList();
        if (dataList.size() > 0) {
            dto.setLastId(dataList.get(dataList.size() - 1).getId());
        }
        return inventoryPageData;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INVENTORY;
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
