package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_WAREHOUSE_SKU;

@Component
@Slf4j
public class ExportOmsWarehouseSkuHandler extends AbstractPageFileEventHandler<SkuMappingDTO.WarehousePagingViewDTO, SkuMappingDTO.ExportWarehouseSkuDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SkuMappingDTO.WarehousePagingViewDTO> getData(FileTask fileTask) {
        SkuMappingDTO.ExportWarehouseSkuDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SkuMappingDTO.ExportWarehouseSkuDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SkuMappingDTO.WarehousePagingViewDTO> getPageData(PagingDTO<SkuMappingDTO.ExportWarehouseSkuDTO> dto) {
        return exportOmsFeign.exportWarehouseSku(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_WAREHOUSE_SKU;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/WarehouseSkuMapping.xlsx";
    }
}
