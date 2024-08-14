package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_SHIPMENT;

@Component
@Slf4j
public class ExportWmsFbaShipmentHandler extends AbstractPageFileEventHandler<FbaShipmentDTO.ExportDTO, FbaShipmentDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/fbaShipment.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FBA_SHIPMENT;
    }

    @Override
    protected List<FbaShipmentDTO.ExportDTO> getData(FileTask fileTask) {
        FbaShipmentDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FbaShipmentDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<FbaShipmentDTO.ExportDTO> getPageData(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportFbaShipment(dto);
    }
}
