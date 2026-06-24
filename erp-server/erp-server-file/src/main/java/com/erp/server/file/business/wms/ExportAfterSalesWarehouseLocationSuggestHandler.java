package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ExportAfterSalesWarehouseLocationSuggestHandler extends AbstractPageFileEventHandler<AfterSalesWarehouseLocationSuggestDto.ListDTO, AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<AfterSalesWarehouseLocationSuggestDto.ListDTO> getPageData(PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> dto) {
        return exportWmsFeign.exportAfterSalesWarehouseLocationSuggest(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/afterSalesWarehouseLocationSuggestExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_WAREHOUSE_LOCATION_SUGGEST_AFTER_SALES;
    }
}