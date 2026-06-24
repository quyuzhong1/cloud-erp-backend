package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.file.entity.FileTask;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

    /**
     * 兼容历史 metaInfo：既可能是 {@link AfterSalesWarehouseLocationSuggestDto.ExportParamDTO}，
     * 也可能是完整 {@link PagingDTO}（含 {@code params} 字段）。
     */
    @Override
    protected AfterSalesWarehouseLocationSuggestDto.ExportParamDTO resolveExportParams(FileTask fileTask) {
        try {
            JsonNode root = getObjectMapper().readTree(fileTask.getMetaInfo());
            AfterSalesWarehouseLocationSuggestDto.ExportParamDTO params;
            if (root.hasNonNull("params")) {
                PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> paging = readValue(
                        fileTask.getMetaInfo(),
                        new TypeReference<PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO>>() {
                        });
                params = paging.getParams();
            } else {
                params = getObjectMapper().convertValue(root, AfterSalesWarehouseLocationSuggestDto.ExportParamDTO.class);
            }
            if (params == null) {
                params = new AfterSalesWarehouseLocationSuggestDto.ExportParamDTO();
            }
            return params;
        } catch (JsonProcessingException e) {
            ServiceException ex = new ServiceException("解析售后仓位推荐导出任务参数失败: " + e.getOriginalMessage());
            ex.initCause(e);
            throw ex;
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