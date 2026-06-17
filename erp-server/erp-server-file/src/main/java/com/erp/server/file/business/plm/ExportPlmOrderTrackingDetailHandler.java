package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ORDER_TRACKING_DETAIL;

@Component
public class ExportPlmOrderTrackingDetailHandler extends AbstractPageFileEventHandler<MouldInfoDTO.OrderTrackingDetailExportDTO, MouldInfoDTO.OrderTrackingDetailParamDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;


    @Override
    protected String getExcelPath() {
        return "excel/plm/orderTrackingDetail.xlsx";
    }

    @Override
    protected PagingVO<MouldInfoDTO.OrderTrackingDetailExportDTO> getPageData(PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto) {
        return exportPlmFeign.exportOrderTrackingDetail(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_ORDER_TRACKING_DETAIL;
    }
}
