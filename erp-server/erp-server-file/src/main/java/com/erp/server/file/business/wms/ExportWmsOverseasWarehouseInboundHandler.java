package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OVERSEAS_WAREHOUSE_INBOUND;

@Component
@Slf4j
public class ExportWmsOverseasWarehouseInboundHandler extends AbstractPageFileEventHandler<OverseasWarehouseInboundDTO.ListDTO, OverseasWarehouseInboundDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/overseasWarehouseInbound.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_OVERSEAS_WAREHOUSE_INBOUND;
    }

    @Override
    protected List<OverseasWarehouseInboundDTO.ListDTO> getData(FileTask fileTask) {
        OverseasWarehouseInboundDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<OverseasWarehouseInboundDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<OverseasWarehouseInboundDTO.ListDTO> getPageData(PagingDTO<OverseasWarehouseInboundDTO.ExportDTO> dto) {
        return exportWmsFeign.exportOverseasWarehouseInbound(dto);
    }
}
