package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationSuggestAfterSalesDto;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WAREHOUSE_LOCATION_SUGGEST_AFTER_SALES;

@Component
@Slf4j
public class ExportWarehouseLocationSuggestAfterSalesHandler extends AbstractPageFileEventHandler<WarehouseLocationSuggestAfterSalesDto.ListDTO, WarehouseLocationSuggestAfterSalesDto.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<WarehouseLocationSuggestAfterSalesDto.ListDTO> getPageData(PagingDTO<WarehouseLocationSuggestAfterSalesDto.SearchParamDTO> dto) {
        return exportWmsFeign.exportWarehouseLocationSuggestAfterSales(dto);
    }

    @Override
    protected List<WarehouseLocationSuggestAfterSalesDto.ListDTO> getData(FileTask fileTask) {
        WarehouseLocationSuggestAfterSalesDto.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<WarehouseLocationSuggestAfterSalesDto.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/warehouseLocationSuggestAfterSalesExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WAREHOUSE_LOCATION_SUGGEST_AFTER_SALES;
    }
}
