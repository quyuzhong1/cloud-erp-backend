package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_SHIPMENT_PACKING;

@Component
@Slf4j
public class ExportWmsFbaShipmentPackingHandler extends AbstractPageFileEventHandler<FbaShipmentPackingDTO.ViewDTO, FbaShipmentDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/fbaShipmentPacking.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FBA_SHIPMENT_PACKING;
    }

    @Override
    protected List<FbaShipmentPackingDTO.ViewDTO> getData(FileTask fileTask) {
        FbaShipmentDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FbaShipmentDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<FbaShipmentPackingDTO.ViewDTO> getPageData(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportFbaShipmentPacking(dto);
    }
}
