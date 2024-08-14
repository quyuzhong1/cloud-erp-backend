package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_B2C_DELIVERY_ORDER;

@Component
@Slf4j
public class ExportWmsB2cDeliveryOrderHandler extends AbstractPageFileEventHandler<SoB2cDeliveryDTO.ListDTO, SoB2cDeliveryDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/b2cDeliveryOrderExport.xlsx";
    }


    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_B2C_DELIVERY_ORDER;
    }

    @Override
    protected List<SoB2cDeliveryDTO.ListDTO> getData(FileTask fileTask) {
        SoB2cDeliveryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2cDeliveryDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2cDeliveryDTO.ListDTO> getPageData(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportB2cDelivery(dto);
    }
}
