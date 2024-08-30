package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_WAREHOUSE_MAPPING;

@Component
@Slf4j
public class ExportTmsWarehouseMappingHandler extends AbstractPageFileEventHandler<TmsWarehouseMappingDTO.ListDTO, TmsWarehouseMappingDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/tmsWarehouseMapping.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_TMS_WAREHOUSE_MAPPING;
    }

    @Override
    protected List<TmsWarehouseMappingDTO.ListDTO> getData(FileTask fileTask) {
        TmsWarehouseMappingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TmsWarehouseMappingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TmsWarehouseMappingDTO.ListDTO> getPageData(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportWarehouseMapping(dto);
    }
}
