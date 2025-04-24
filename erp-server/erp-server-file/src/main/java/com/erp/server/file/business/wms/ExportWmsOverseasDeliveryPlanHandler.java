package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OVERSEAS_DELIVERY_PLAN;

@Component
@Slf4j
public class ExportWmsOverseasDeliveryPlanHandler extends AbstractPageFileEventHandler<WmsDeliveryPlanDTO.ListDTO, WmsDeliveryPlanDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/overseasDeliveryPlan.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_OVERSEAS_DELIVERY_PLAN;
    }

    @Override
    protected List<WmsDeliveryPlanDTO.ListDTO> getData(FileTask fileTask) {
        WmsDeliveryPlanDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference< WmsDeliveryPlanDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<WmsDeliveryPlanDTO.ListDTO> getPageData(PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportOverseasDeliveryPlan(dto);
    }
}
