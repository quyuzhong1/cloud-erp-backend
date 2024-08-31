package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_DELIVERY;

@Component
@Slf4j
public class ExportWmsFbaDeliveryHandler extends AbstractPageFileEventHandler<FirstMileDeliveryDTO.ListDTO, FirstMileDeliveryDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/fbaDelivery.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FBA_DELIVERY;
    }

    @Override
    protected List<FirstMileDeliveryDTO.ListDTO> getData(FileTask fileTask) {
        FirstMileDeliveryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FirstMileDeliveryDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<FirstMileDeliveryDTO.ListDTO> getPageData(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportFbaDelivery(dto);
    }
}
