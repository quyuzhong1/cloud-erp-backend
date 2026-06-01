package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.enums.inventory.InventorySearchDimensionEnum;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.common.business.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY;

@Component
@Slf4j
public class ExportWmsInventoryHandler extends AbstractPageFileEventHandler<InventoryDTO.PagingViewDTO, InventoryDTO.ExportSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<InventoryDTO.PagingViewDTO> getPageData(PagingDTO<InventoryDTO.ExportSearchParamDTO> dto) {
        PagingVO<InventoryDTO.PagingViewDTO> inventoryPageData = exportWmsFeign.getInventoryPageData(dto);
        List<InventoryDTO.PagingViewDTO> dataList = (List<InventoryDTO.PagingViewDTO>) inventoryPageData.getList();
        if (CollectionUtils.isNotEmpty(dataList)) {
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
        throw new UnsupportedOperationException("分页导出请使用 getExcelPath(P)");
    }

    @Override
    protected String getExcelPath(InventoryDTO.ExportSearchParamDTO param) {
        String dimension = param == null ? null : param.getDimension();
        boolean includeSupplier = param != null && Boolean.TRUE.equals(param.getIncludeSupplier());
        if (Objects.equals(dimension, InventorySearchDimensionEnum.WAREHOUSE.getCode())) {
            return includeSupplier ? "excel/wms/inventoryIncludeSupplier.xlsx" : "excel/wms/inventory.xlsx";
        }
        if (Objects.equals(dimension, InventorySearchDimensionEnum.WAREHOUSE_AREA.getCode())) {
            return includeSupplier ? "excel/wms/inventory_area_include_supplier.xlsx" : "excel/wms/inventory_area.xlsx";
        }
        if (Objects.equals(dimension, InventorySearchDimensionEnum.WAREHOUSE_LOCATION.getCode())) {
            return includeSupplier ? "excel/wms/inventory_location_include_supplier.xlsx" : "excel/wms/inventory_location.xlsx";
        }
        throw new ServiceException("库存导出维度不支持: " + dimension);
    }
}
