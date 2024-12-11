package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ORDER_TRACKING;

@Component
public class ExportPlmOrderTrackingHandler extends AbstractPageFileEventHandler<MouldInfoDTO.OrderTrackingExportDTO, MouldInfoDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected List<MouldInfoDTO.OrderTrackingExportDTO> getData(FileTask fileTask) {
        MouldInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<MouldInfoDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/orderTracking.xlsx";
    }

    @Override
    protected PagingVO<MouldInfoDTO.OrderTrackingExportDTO> getPageData(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportOrderTracking(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_ORDER_TRACKING;
    }
}
