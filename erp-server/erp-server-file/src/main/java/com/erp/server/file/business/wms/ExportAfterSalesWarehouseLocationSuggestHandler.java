package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class ExportAfterSalesWarehouseLocationSuggestHandler extends AbstractPageFileEventHandler<AfterSalesWarehouseLocationSuggestDto.ListDTO, AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Resource
    private ObjectMapper objectMapper;

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
