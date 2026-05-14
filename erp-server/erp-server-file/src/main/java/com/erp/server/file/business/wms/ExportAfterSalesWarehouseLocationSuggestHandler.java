package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
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
    protected List<AfterSalesWarehouseLocationSuggestDto.ListDTO> getData(FileTask fileTask) {
        try {
            JsonNode root = objectMapper.readTree(fileTask.getMetaInfo());
            AfterSalesWarehouseLocationSuggestDto.ExportParamDTO params;
            if (root.hasNonNull("params")) {
                PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> paging =
                        objectMapper.convertValue(root, new TypeReference<PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO>>() {
                        });
                params = paging.getParams();
            } else {
                params = objectMapper.convertValue(root, AfterSalesWarehouseLocationSuggestDto.ExportParamDTO.class);
            }
            if (params == null) {
                params = new AfterSalesWarehouseLocationSuggestDto.ExportParamDTO();
            }
            return listSeqData(params);
        } catch (Exception e) {
            throw new ServiceException("解析售后仓位推荐导出任务参数失败: " + e.getMessage());
        }
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
